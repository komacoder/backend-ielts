package com.ieltsai.ielts_ai_backend.dto.writing;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaScoresDto {
    private Double taskResponseOrAchievement;
    private Double coherenceAndCohesion;
    private Double lexicalResource;
    private Double grammaticalRangeAndAccuracy;
}
