package com.ieltsai.ielts_ai_backend.dto.speaking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingSessionResponseDto {
    private Long sessionId;
    private Long userId;
    private String status;
    private String generatedQuestionsJson;
}
