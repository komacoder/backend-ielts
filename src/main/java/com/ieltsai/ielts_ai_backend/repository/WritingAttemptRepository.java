package com.ieltsai.ielts_ai_backend.repository;

import com.ieltsai.ielts_ai_backend.entity.User;
import com.ieltsai.ielts_ai_backend.entity.WritingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WritingAttemptRepository extends JpaRepository<WritingAttempt, Long> {

    List<WritingAttempt> findAllByUserOrderBySubmittedAtDesc(User user);

    Optional<WritingAttempt> findByIdAndUser(Long id, User user);
}
