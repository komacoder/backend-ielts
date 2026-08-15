package com.ieltsai.ielts_ai_backend.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriterionAnalysisDto {
    private Double score;
    private String analysis;
}
