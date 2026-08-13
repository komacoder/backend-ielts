package com.ieltsai.ielts_ai_backend.controller;

import com.ieltsai.ielts_ai_backend.dto.writing.CreateTopicRequest;
import com.ieltsai.ielts_ai_backend.dto.writing.TopicResponse;
import com.ieltsai.ielts_ai_backend.dto.writing.UpdateTopicRequest;
import com.ieltsai.ielts_ai_backend.service.WritingTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/writing/topics")
@RequiredArgsConstructor
@Tag(name = "3. \uD83D\uDD27 Admin Topic Management", description = "Admin endpoints for creating, updating, listing, and deleting writing topics")
public class AdminWritingTopicController {

    private final WritingTopicService writingTopicService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all topics (including inactive)", description = "Returns all writing topics in the system, regardless of active status. Requires ADMIN role.")
    public ResponseEntity<List<TopicResponse>> getAllTopics() {
        return ResponseEntity.ok(writingTopicService.getAllTopics());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new writing topic", description = "Creates a new topic in the writing bank. Requires ADMIN role.")
    public ResponseEntity<TopicResponse> createTopic(
            @Valid @RequestBody CreateTopicRequest request
    ) {
        TopicResponse created = writingTopicService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing writing topic", description = "Updates an existing topic in the writing bank. Requires ADMIN role.")
    public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTopicRequest request
    ) {
        return ResponseEntity.ok(writingTopicService.updateTopic(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a writing topic", description = "Deletes a topic from the writing bank by ID. Requires ADMIN role.")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        writingTopicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }
}
