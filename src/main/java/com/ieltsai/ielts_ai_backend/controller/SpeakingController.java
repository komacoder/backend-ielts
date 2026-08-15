package com.ieltsai.ielts_ai_backend.controller;

import com.ieltsai.ielts_ai_backend.entity.SpeakingPart;
import com.ieltsai.ielts_ai_backend.dto.speaking.SpeakingEvaluationResponseDto;
import com.ieltsai.ielts_ai_backend.dto.speaking.SpeakingSessionResponseDto;
import com.ieltsai.ielts_ai_backend.service.SpeakingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/speaking")
@Tag(name = "Speaking Evaluation", description = "Endpoints for IELTS Speaking test evaluation")
public class SpeakingController {

    private final SpeakingService speakingService;

    public SpeakingController(SpeakingService speakingService) {
        this.speakingService = speakingService;
    }

    @PostMapping("/sessions/start")
    @Operation(summary = "Start a new speaking session",
               description = "Generates IELTS Speaking Part 1, 2, 3 questions via Gemini and creates a session")
    public ResponseEntity<SpeakingSessionResponseDto> startSession(@RequestParam Long userId) {
        SpeakingSessionResponseDto response = speakingService.startSession(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/sessions/{sessionId}/submit-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit audio answer for evaluation",
               description = "Evaluates submitted audio (.ogg, .webm, .mp3) using Gemini Multimodal API")
    public ResponseEntity<SpeakingEvaluationResponseDto> submitAudioAnswer(
            @PathVariable Long sessionId,
            @RequestParam SpeakingPart part,
            @RequestPart("file") MultipartFile file) {
        SpeakingEvaluationResponseDto response = speakingService.submitAudioAnswer(sessionId, part, file);
        return ResponseEntity.ok(response);
    }
}
