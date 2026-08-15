package com.ieltsai.ielts_ai_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsai.ielts_ai_backend.dto.speaking.SpeakingEvaluationResponseDto;
import com.ieltsai.ielts_ai_backend.dto.speaking.SpeakingSessionResponseDto;
import com.ieltsai.ielts_ai_backend.entity.SpeakingPart;
import com.ieltsai.ielts_ai_backend.entity.SpeakingSession;
import com.ieltsai.ielts_ai_backend.entity.SpeakingSubmission;
import com.ieltsai.ielts_ai_backend.entity.User;
import com.ieltsai.ielts_ai_backend.repository.SpeakingSessionRepository;
import com.ieltsai.ielts_ai_backend.repository.SpeakingSubmissionRepository;
import com.ieltsai.ielts_ai_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class SpeakingService {

    private final SpeakingSessionRepository sessionRepository;
    private final SpeakingSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    public SpeakingService(SpeakingSessionRepository sessionRepository,
                           SpeakingSubmissionRepository submissionRepository,
                           UserRepository userRepository,
                           GeminiApiClient geminiApiClient,
                           ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.geminiApiClient = geminiApiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Starts a new IELTS Speaking session for the given user.
     * Calls Gemini to generate Part 1, Part 2, and Part 3 questions,
     * persists the session, and returns the session details.
     */
    @Transactional
    public SpeakingSessionResponseDto startSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        log.info("Starting speaking session for userId={}", userId);
        String generatedQuestionsJson = geminiApiClient.generateSpeakingQuestions();

        SpeakingSession session = new SpeakingSession();
        session.setUser(user);
        session.setStatus("IN_PROGRESS");
        session.setGeneratedQuestionsJson(generatedQuestionsJson);
        session = sessionRepository.save(session);

        log.info("Speaking session created: sessionId={}", session.getId());

        return new SpeakingSessionResponseDto(
                session.getId(),
                user.getId(),
                session.getStatus(),
                session.getGeneratedQuestionsJson()
        );
    }

    /**
     * Submits an audio answer for a specific part of the speaking test.
     * Validates the session/file, calls Gemini multimodal for evaluation,
     * persists the submission, and updates session status if all parts are done.
     */
    @Transactional
    public SpeakingEvaluationResponseDto submitAudioAnswer(Long sessionId, SpeakingPart part, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Audio file cannot be empty");
        }

        SpeakingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + sessionId));

        String mimeType = determineMimeType(file);
        log.info("Submitting audio for sessionId={}, part={}, mimeType={}, fileSize={}",
                sessionId, part, mimeType, file.getSize());

        try {
            byte[] audioBytes = file.getBytes();
            String taskContext = "Evaluating IELTS Speaking " + part.name().replace("_", " ")
                    + ". Assess the candidate's spoken response appropriately for this section.";

            SpeakingEvaluationResponseDto evaluation =
                    geminiApiClient.evaluateSpeakingAudio(audioBytes, mimeType, taskContext);

            // Persist submission
            SpeakingSubmission submission = new SpeakingSubmission();
            submission.setSession(session);
            submission.setPart(part);
            submission.setOverallBandScore(evaluation.getOverallBandScore());
            submission.setEvaluationJson(objectMapper.writeValueAsString(evaluation));
            submissionRepository.save(submission);

            // Check if all 3 parts have been submitted -> mark session COMPLETED
            List<SpeakingSubmission> allSubmissions = submissionRepository.findBySessionId(sessionId);
            long distinctParts = allSubmissions.stream()
                    .map(SpeakingSubmission::getPart)
                    .distinct()
                    .count();
            if (distinctParts >= SpeakingPart.values().length) {
                session.setStatus("COMPLETED");
                sessionRepository.save(session);
                log.info("Session {} marked as COMPLETED (all parts submitted)", sessionId);
            }

            return evaluation;
        } catch (Exception e) {
            log.error("Failed to evaluate audio for sessionId={}, part={}: {}", sessionId, part, e.getMessage());
            throw new RuntimeException("Failed to evaluate speaking audio: " + e.getMessage(), e);
        }
    }

    /**
     * Determines the MIME type from file extension first, then falls back
     * to the content-type reported by the browser. Defaults to audio/ogg.
     */
    private String determineMimeType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            if (lower.endsWith(".ogg")) return "audio/ogg";
            if (lower.endsWith(".webm")) return "audio/webm";
            if (lower.endsWith(".mp3")) return "audio/mp3";
            if (lower.endsWith(".wav")) return "audio/wav";
        }
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("audio/")) {
            return contentType;
        }
        // Safe default for Gemini multimodal
        return "audio/ogg";
    }
}
