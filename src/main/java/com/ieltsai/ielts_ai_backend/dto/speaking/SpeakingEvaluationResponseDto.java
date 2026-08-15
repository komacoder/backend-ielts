package com.ieltsai.ielts_ai_backend.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingEvaluationResponseDto {
    private CriteriaAnalysisDto criteriaAnalysis;
    private Double overallBandScore;
    private List<NotableSentenceDto> notableSentences;
}
