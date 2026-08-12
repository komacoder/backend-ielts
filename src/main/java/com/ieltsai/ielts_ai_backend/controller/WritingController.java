package com.ieltsai.ielts_ai_backend.controller;

import com.ieltsai.ielts_ai_backend.dto.writing.EssayFeedbackResponse;
import com.ieltsai.ielts_ai_backend.dto.writing.QuestionResponse;
import com.ieltsai.ielts_ai_backend.dto.writing.SubmitEssayRequest;
import com.ieltsai.ielts_ai_backend.entity.User;
import com.ieltsai.ielts_ai_backend.service.WritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-facing REST controller for the IELTS Writing module.
 *
 * All endpoints require authentication (JWT Bearer token).
 * The authenticated User principal is injected via @AuthenticationPrincipal
 * to scope all data operations to the currently logged-in user.
 */
@RestController
@RequestMapping("/api/v1/writing")
@RequiredArgsConstructor
public class WritingController {

    private final WritingService writingService;

    /**
     * GET /api/v1/writing/questions
     * Returns all active writing questions available to the user.
     */
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionResponse>> getActiveQuestions() {
        return ResponseEntity.ok(writingService.getActiveQuestions());
    }

    /**
     * POST /api/v1/writing/submit
     * Submits an essay for AI evaluation. Validates word count, calls Gemini,
     * persists the result, and returns full structured feedback.
     */
    @PostMapping("/submit")
    public ResponseEntity<EssayFeedbackResponse> submitEssay(
            @Valid @RequestBody SubmitEssayRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(writingService.submitEssay(request, currentUser));
    }

    /**
     * GET /api/v1/writing/attempts/{id}
     * Retrieves a specific writing attempt by ID. Access is automatically scoped
     * to the authenticated user — other users' attempts cannot be accessed.
     */
    @GetMapping("/attempts/{id}")
    public ResponseEntity<EssayFeedbackResponse> getAttempt(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(writingService.getAttemptById(id, currentUser));
    }
}
