package com.ieltsai.ielts_ai_backend.controller;

import com.ieltsai.ielts_ai_backend.dto.writing.WritingEvaluationResponseDto;
import com.ieltsai.ielts_ai_backend.dto.writing.WritingSubmissionRequestDto;
import com.ieltsai.ielts_ai_backend.entity.User;
import com.ieltsai.ielts_ai_backend.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/writing/submissions")
@RequiredArgsConstructor
@Tag(name = "Writing Submissions", description = "Endpoints for submitting essays, viewing submission details, and managing history")
public class WritingSubmissionController {

    private final WritingService writingService;

    @PostMapping("/submit")
    @Operation(summary = "Submit an essay", description = "Submits an essay for AI evaluation. Validates word count, calls Gemini, persists the result, and returns full structured feedback.")
    public ResponseEntity<WritingEvaluationResponseDto> submitEssay(
            @Valid @RequestBody WritingSubmissionRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(writingService.submitEssay(request, currentUser));
    }

    @GetMapping("/history")
    @Operation(summary = "Get user submission history", description = "Returns the authenticated user's writing attempt history, ordered by most recent first.")
    public ResponseEntity<List<WritingEvaluationResponseDto>> getUserHistory(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(writingService.getUserHistory(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific submission", description = "Retrieves a specific writing attempt by ID. Access is scoped to the authenticated user.")
    public ResponseEntity<WritingEvaluationResponseDto> getAttempt(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(writingService.getAttemptById(id, currentUser));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a submission", description = "Deletes a specific writing attempt by ID from the user's history.")
    public ResponseEntity<Void> deleteSubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        writingService.deleteSubmission(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
