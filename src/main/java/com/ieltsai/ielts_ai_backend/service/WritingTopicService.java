package com.ieltsai.ielts_ai_backend.service;

import com.ieltsai.ielts_ai_backend.dto.writing.CreateTopicRequest;
import com.ieltsai.ielts_ai_backend.dto.writing.TopicResponse;
import com.ieltsai.ielts_ai_backend.dto.writing.UpdateTopicRequest;
import com.ieltsai.ielts_ai_backend.entity.QuestionBank;
import com.ieltsai.ielts_ai_backend.repository.QuestionBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingTopicService {

    private final QuestionBankRepository questionBankRepository;

    /**
     * Returns all active topics visible to users.
     */
    @Transactional(readOnly = true)
    public List<TopicResponse> getActiveTopics() {
        return questionBankRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::toTopicResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all topics including inactive ones (Admin).
     */
    @Transactional(readOnly = true)
    public List<TopicResponse> getAllTopics() {
        return questionBankRepository.findAll()
                .stream()
                .map(this::toTopicResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single topic by ID.
     */
    @Transactional(readOnly = true)
    public TopicResponse getTopicById(Long id) {
        QuestionBank topic = questionBankRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic with id=" + id + " not found."));
        return toTopicResponse(topic);
    }

    /**
     * Admin: creates a new topic in the bank.
     */
    @Transactional
    public TopicResponse createTopic(CreateTopicRequest request) {
        QuestionBank topic = QuestionBank.builder()
                .topicTitle(request.title())
                .topicPrompt(request.promptText())
                .taskType(request.taskType())
                .isActive(true)
                .build();
        QuestionBank saved = questionBankRepository.save(topic);
        log.info("Created new topic with id={} taskType={}", saved.getId(), saved.getTaskType());
        return toTopicResponse(saved);
    }

    /**
     * Admin: updates an existing topic.
     */
    @Transactional
    public TopicResponse updateTopic(Long id, UpdateTopicRequest request) {
        QuestionBank topic = questionBankRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic with id=" + id + " not found."));

        if (request.title() != null) topic.setTopicTitle(request.title());
        if (request.promptText() != null) topic.setTopicPrompt(request.promptText());
        if (request.taskType() != null) topic.setTaskType(request.taskType());
        if (request.isActive() != null) topic.setActive(request.isActive());

        QuestionBank saved = questionBankRepository.save(topic);
        log.info("Updated topic with id={}", saved.getId());
        return toTopicResponse(saved);
    }

    /**
     * Admin: deletes a topic by ID.
     */
    @Transactional
    public void deleteTopic(Long id) {
        if (!questionBankRepository.existsById(id)) {
            throw new IllegalArgumentException("Topic with id=" + id + " not found.");
        }
        questionBankRepository.deleteById(id);
        log.info("Deleted topic with id={}", id);
    }

    private TopicResponse toTopicResponse(QuestionBank q) {
        return new TopicResponse(q.getId(), q.getTopicTitle(), q.getTopicPrompt(), q.getTaskType(), q.isActive(), q.getCreatedAt());
    }
}
