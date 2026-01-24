package com.example.controller;

import com.example.dto.EmailRequest;
import com.example.dto.ErrorResponse;
import com.example.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "API для отправки email уведомлений")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Отправить email",
            description = "Отправляет пользовательский email на указанный адрес"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email успешно отправлен",
                    content = @Content(mediaType = "text/plain")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные (неправильный email или пустые поля)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка при отправке email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.OK)
    public String sendEmail(
            @Parameter(
                    description = "Данные для отправки email",
                    required = true,
                    schema = @Schema(implementation = EmailRequest.class)
            )
            @Valid @RequestBody EmailRequest request) {
        notificationService.sendCustomEmail(
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );

        return "Email sent successfully to " + request.getEmail();
    }
}
