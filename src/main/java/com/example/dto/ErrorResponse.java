package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Структура ошибки API")
public class ErrorResponse {

    @Schema(description = "Время возникновения ошибки", example = "2024-01-23T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус код", example = "400")
    private int status;

    @Schema(description = "Описание ошибки", example = "Bad Request")
    private String error;

    @Schema(description = "Сообщение об ошибке", example = "Email is required")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/notifications/send")
    private String path;

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
