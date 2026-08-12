package com.ieltsai.ielts_ai_backend.repository;

import com.ieltsai.ielts_ai_backend.entity.QuestionBank;
import com.ieltsai.ielts_ai_backend.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {

    List<QuestionBank> findAllByIsActiveTrue();

    List<QuestionBank> findAllByIsActiveTrueAndTaskType(TaskType taskType);
}
