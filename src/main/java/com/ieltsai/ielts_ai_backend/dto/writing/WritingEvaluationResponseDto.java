package com.ieltsai.ielts_ai_backend.dto.writing;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingEvaluationResponseDto {
    private Long attemptId;
    private Long questionId;
    private String questionTitle;
    private Integer wordCount;
    private Double overallBand;
    private CriteriaScoresDto criteriaScores;
    private String summary;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<WritingErrorDto> errors;
    private List<String> recommendations;
    private LocalDateTime submittedAt;
}
