package com.ieltsai.ielts_ai_backend.controller;

import com.ieltsai.ielts_ai_backend.dto.writing.CreateQuestionRequest;
import com.ieltsai.ielts_ai_backend.dto.writing.QuestionResponse;
import com.ieltsai.ielts_ai_backend.service.WritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only REST controller for managing the IELTS Writing question bank.
 *
 * Method-level security via @PreAuthorize ensures only users with the ADMIN role
 * can access these endpoints, on top of the URL-level restrictions in SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/admin/writing")
@RequiredArgsConstructor
public class AdminWritingController {

    private final WritingService writingService;

    /**
     * POST /api/v1/admin/writing/questions
     * Creates a new writing question. Admin role required.
     */
    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        QuestionResponse created = writingService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
