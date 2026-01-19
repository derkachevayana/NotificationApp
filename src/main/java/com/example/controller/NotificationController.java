package com.example.controller;

import com.example.dto.EmailRequest;
import com.example.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.OK)
    public String sendEmail(@Valid @RequestBody EmailRequest request) {
        notificationService.sendCustomEmail(
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );

        return "Email sent successfully to " + request.getEmail();
    }
}
