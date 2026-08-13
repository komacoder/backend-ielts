package com.ieltsai.ielts_ai_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsai.ielts_ai_backend.dto.writing.GeminiEvaluationResult;
import com.ieltsai.ielts_ai_backend.entity.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Handles all communication with the Google Gemini API.
 *
 * DESIGN PRINCIPLES:
 * 1. ANTI-HARDCODING: All config values (base URL, API key, model, temperature,
 *    response MIME type) are injected from application.yml via @Value.
 * 2. ISOLATION: Uses a manually-built RestClient that does NOT inherit any
 *    global Spring Security/OAuth2 interceptors — preventing 401 errors.
 * 3. PROMPT INJECTION PROTECTION: Prompt construction is delegated to
 *    WritingPromptBuilder, which uses XML delimiters to isolate user content.
 */
@Slf4j
@Service
public class GeminiApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final WritingPromptBuilder promptBuilder;
    private final String model;
    private final String apiKey;
    private final double temperature;
    private final String responseMimeType;

    public GeminiApiClient(
            @Value("${gemini.api.base-url}") String baseUrl,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String model,
            @Value("${gemini.api.generation.temperature}") double temperature,
            @Value("${gemini.api.generation.response-mime-type}") String responseMimeType,
            WritingPromptBuilder promptBuilder
    ) {
        this.model = model;
        this.apiKey = apiKey;
        this.temperature = temperature;
        this.responseMimeType = responseMimeType;
        this.promptBuilder = promptBuilder;
        this.objectMapper = new ObjectMapper();

        // Isolated RestClient — no Spring-managed builder, no global interceptors
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    /**
     * Evaluates the given essay using the Gemini model.
     *
     * @param essay          The raw essay text submitted by the user.
     * @param questionPrompt The question prompt text shown to the user.
     * @param taskType       The IELTS task type (TASK_1 or TASK_2).
     * @return               A structured {@link GeminiEvaluationResult}.
     */
    public GeminiEvaluationResult evaluate(String essay, String questionPrompt, TaskType taskType) {
        String systemInstruction = promptBuilder.getSystemInstruction() + """
                
                ## OUTPUT FORMAT
                You MUST respond with a single, valid JSON object matching this exact schema. Do not include any
                text, markdown, or explanation outside the JSON object:
                
                {
                  "overallBand": <number with one decimal, e.g. 6.5>,
                  "criteriaScores": {
                    "taskResponseOrAchievement": <0.0–9.0>,
                    "coherenceAndCohesion": <0.0–9.0>,
                    "lexicalResource": <0.0–9.0>,
                    "grammaticalRangeAndAccuracy": <0.0–9.0>
                  },
                  "summary": "<overall summary of performance>",
                  "strengths": [
                    "<strength 1>",
                    "<strength 2>"
                  ],
                  "weaknesses": [
                    "<weakness 1>",
                    "<weakness 2>"
                  ],
                  "errors": [
                    {
                      "originalText": "<exact excerpt from the essay>",
                      "correction": "<suggested corrected version>",
                      "explanation": "<clear explanation of the error>",
                      "feedbackType": "<one of: GRAMMAR, LEXICAL, COHERENCE, TASK_RESPONSE>"
                    }
                  ],
                  "recommendations": [
                    "<actionable improvement tip 1>",
                    "<actionable improvement tip 2>"
                  ]
                }
                
                The overallBand must be the mean of the four criteriaScores, rounded to the nearest 0.5.
                Identify at least 3 and at most 10 errors. Provide 3–5 recommendations.
                """;
        String userMessage = promptBuilder.buildUserMessage(essay, questionPrompt, taskType);

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", userMessage))
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", responseMimeType,
                        "temperature", temperature
                )
        );

        try {
            String rawResponse = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        String errorBody = new String(response.getBody().readAllBytes());
                        log.error("Gemini API error: status={}, body={}", response.getStatusCode(), errorBody);
                        throw new RuntimeException(
                                "Gemini API request failed with status " + response.getStatusCode() + ": " + errorBody
                        );
                    })
                    .body(String.class);

            return parseGeminiResponse(rawResponse);
        } catch (RestClientResponseException e) {
            log.error("Gemini API connection error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the JSON payload from Gemini's response envelope, strips any
     * residual markdown wrappers, and maps it to {@link GeminiEvaluationResult}.
     */
    private GeminiEvaluationResult parseGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String jsonText = root
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Strip markdown code-block wrappers if present (```json ... ```)
            jsonText = stripMarkdownWrapper(jsonText);

            return objectMapper.readValue(jsonText, GeminiEvaluationResult.class);
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", rawResponse);
            throw new RuntimeException(
                    "Could not parse Gemini AI evaluation response. Raw response: " + rawResponse, e
            );
        }
    }

    /**
     * Removes markdown code-block fences (```json / ```) that Gemini may
     * occasionally wrap around JSON output despite instructions not to.
     */
    private String stripMarkdownWrapper(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
