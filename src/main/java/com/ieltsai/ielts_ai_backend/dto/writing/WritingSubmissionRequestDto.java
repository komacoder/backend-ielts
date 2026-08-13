package com.ieltsai.ielts_ai_backend.dto.writing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WritingSubmissionRequestDto {

    @NotNull(message = "Topic ID must be provided")
    private Long topicId;

    @NotBlank(message = "Essay text must not be blank")
    private String essayText;
}
