package com.ieltsai.ielts_ai_backend.repository;

import com.ieltsai.ielts_ai_backend.entity.SpeakingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpeakingSubmissionRepository extends JpaRepository<SpeakingSubmission, Long> {
    List<SpeakingSubmission> findBySessionId(Long sessionId);
}
