package com.ieltsai.ielts_ai_backend.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaAnalysisDto {
    private CriterionAnalysisDto fluencyAndCoherence;
    private CriterionAnalysisDto lexicalResource;
    private CriterionAnalysisDto grammaticalRangeAndAccuracy;
    private CriterionAnalysisDto pronunciation;
}
