package com.ieltsai.ielts_ai_backend.controller;

import com.ieltsai.ielts_ai_backend.dto.writing.TopicResponse;
import com.ieltsai.ielts_ai_backend.service.WritingTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/writing/topics")
@RequiredArgsConstructor
@Tag(name = "2. \uD83D\uDCDA Writing Topics", description = "Endpoints for exploring available writing topics")
public class WritingTopicController {

    private final WritingTopicService writingTopicService;

    @GetMapping
    @Operation(summary = "Get active topics", description = "Returns all active writing topics available to users.")
    public ResponseEntity<List<TopicResponse>> getActiveTopics() {
        return ResponseEntity.ok(writingTopicService.getActiveTopics());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific topic", description = "Retrieves details of a specific writing topic by its ID.")
    public ResponseEntity<TopicResponse> getTopic(@PathVariable Long id) {
        return ResponseEntity.ok(writingTopicService.getTopicById(id));
    }
}
