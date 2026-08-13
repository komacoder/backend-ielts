package com.ieltsai.ielts_ai_backend.dto.writing;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingErrorDto {
    private String originalText;
    private String correction;
    private String explanation;
    private String feedbackType;
}
