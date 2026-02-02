package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на отправку email")
public class EmailRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email получателя", example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Subject is required")
    @Schema(description = "Тема письма", example = "Важное уведомление",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @NotBlank(message = "Message is required")
    @Schema(description = "Текст сообщения", example = "Здравствуйте! Это тестовое сообщение.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
}
