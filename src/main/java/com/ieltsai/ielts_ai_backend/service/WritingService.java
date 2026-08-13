package com.ieltsai.ielts_ai_backend.service;

import com.ieltsai.ielts_ai_backend.dto.writing.*;
import com.ieltsai.ielts_ai_backend.entity.QuestionBank;
import com.ieltsai.ielts_ai_backend.entity.TaskType;
import com.ieltsai.ielts_ai_backend.entity.User;
import com.ieltsai.ielts_ai_backend.entity.WritingAttempt;
import com.ieltsai.ielts_ai_backend.repository.QuestionBankRepository;
import com.ieltsai.ielts_ai_backend.repository.WritingAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingService {

    private static final int TASK_1_MIN_WORDS = 150;
    private static final int TASK_2_MIN_WORDS = 250;

    private final QuestionBankRepository questionBankRepository;
    private final WritingAttemptRepository writingAttemptRepository;
    private final GeminiApiClient geminiApiClient;

    // ─────────────────────────────────────────────────────────────────
    // Question Management
    // ─────────────────────────────────────────────────────────────────

    /**
     * Returns all active questions visible to users.
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getActiveQuestions() {
        return questionBankRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::toQuestionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin: creates a new question in the question bank.
     */
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        QuestionBank question = QuestionBank.builder()
                .title(request.title())
                .promptText(request.promptText())
                .taskType(request.taskType())
                .isActive(true)
                .build();
        QuestionBank saved = questionBankRepository.save(question);
        log.info("Created new question with id={} taskType={}", saved.getId(), saved.getTaskType());
        return toQuestionResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────
    // Essay Submission & Evaluation Pipeline
    // ─────────────────────────────────────────────────────────────────

    /**
     * Full essay evaluation pipeline:
     * 1. Validate that the question exists and is active.
     * 2. Validate minimum word count per task type.
     * 3. Invoke Gemini AI for evaluation.
     * 4. Persist the attempt with scores and feedback.
     * 5. Return the structured feedback response to the caller.
     */
    @Transactional
    public WritingEvaluationResponseDto submitEssay(WritingSubmissionRequestDto request, User user) {
        QuestionBank question = questionBankRepository.findById(request.getQuestionId())
                .filter(QuestionBank::isActive)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question with id=" + request.getQuestionId() + " not found or is not active."
                ));

        String essayText = request.getEssayText().trim();
        int wordCount = countWords(essayText);
        validateWordCount(wordCount, question.getTaskType());

        log.info("Sending essay for evaluation: userId={} questionId={} wordCount={} taskType={}",
                user.getId(), question.getId(), wordCount, question.getTaskType());

        GeminiEvaluationResult evaluation = geminiApiClient.evaluate(
                essayText,
                question.getPromptText(),
                question.getTaskType()
        );

        // Map Gemini result to persistence structures
        WritingFeedbackData feedbackData = WritingFeedbackData.builder()
                .summary(evaluation.summary())
                .strengths(evaluation.strengths())
                .weaknesses(evaluation.weaknesses())
                .errors(evaluation.errors())
                .recommendations(evaluation.recommendations())
                .build();

        WritingAttempt attempt = WritingAttempt.builder()
                .user(user)
                .question(question)
                .essayText(essayText)
                .wordCount(wordCount)
                .overallBand(evaluation.overallBand())
                .criteriaScores(evaluation.criteriaScores())
                .feedbackDetails(feedbackData)
                .build();

        WritingAttempt saved = writingAttemptRepository.save(attempt);
        log.info("Saved writing attempt id={} for userId={} with band={}",
                saved.getId(), user.getId(), saved.getOverallBand());

        return toEvaluationResponse(saved, question);
    }

    /**
     * Retrieves a specific attempt by ID, scoped to the authenticated user
     * to prevent IDOR (Insecure Direct Object Reference) vulnerabilities.
     */
    @Transactional(readOnly = true)
    public WritingEvaluationResponseDto getAttemptById(Long attemptId, User user) {
        WritingAttempt attempt = writingAttemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Writing attempt with id=" + attemptId + " not found for current user."
                ));
        return toEvaluationResponse(attempt, attempt.getQuestion());
    }

    /**
     * Retrieves the full writing history for the authenticated user,
     * ordered by most recent submission first.
     */
    @Transactional(readOnly = true)
    public List<WritingEvaluationResponseDto> getUserHistory(User user) {
        return writingAttemptRepository.findAllByUserOrderBySubmittedAtDesc(user)
                .stream()
                .map(attempt -> toEvaluationResponse(attempt, attempt.getQuestion()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Counts words by splitting on one or more whitespace characters.
     */
    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return Arrays.stream(text.trim().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .mapToInt(w -> 1)
                .sum();
    }

    private void validateWordCount(int wordCount, TaskType taskType) {
        int minimum = taskType == TaskType.TASK_1 ? TASK_1_MIN_WORDS : TASK_2_MIN_WORDS;
        if (wordCount < minimum) {
            throw new IllegalArgumentException(String.format(
                    "Essay is too short. %s requires a minimum of %d words. Submitted: %d words.",
                    taskType.name().replace('_', ' '), minimum, wordCount
            ));
        }
    }

    private QuestionResponse toQuestionResponse(QuestionBank q) {
        return new QuestionResponse(q.getId(), q.getTitle(), q.getPromptText(), q.getTaskType(), q.getCreatedAt());
    }

    private WritingEvaluationResponseDto toEvaluationResponse(WritingAttempt attempt, QuestionBank question) {
        WritingFeedbackData fd = attempt.getFeedbackDetails();

        return WritingEvaluationResponseDto.builder()
                .attemptId(attempt.getId())
                .questionId(question.getId())
                .questionTitle(question.getTitle())
                .wordCount(attempt.getWordCount())
                .overallBand(attempt.getOverallBand())
                .criteriaScores(attempt.getCriteriaScores())
                .summary(fd.getSummary())
                .strengths(fd.getStrengths())
                .weaknesses(fd.getWeaknesses())
                .errors(fd.getErrors())
                .recommendations(fd.getRecommendations())
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }
}
