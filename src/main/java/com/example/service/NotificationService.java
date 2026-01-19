package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    public void sendWelcomeEmail(String email, String userName) {
        if (email == null || email.isBlank()) {
            log.warn("Cannot send welcome email: email is empty");
            return;
        }
        String subject = "Добро пожаловать!";
        String text = String.format("""
                Здравствуйте%s!
                            
                Ваш аккаунт на сайте был успешно создан.
                            
                С уважением,
                Команда сайта
                """, userName != null ? ", " + userName : "");

        try {
            emailService.sendEmail(email, subject, text);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
        }
    }

    public void sendGoodbyeEmail(String email, String userName) {
        if (email == null || email.isBlank()) {
            log.warn("Cannot send goodbye email: email is empty");
            return;
        }
        String subject = "Ваш аккаунт удален";
        String text = String.format("""
            Здравствуйте%s!
            
            Ваш аккаунт был удален.
            
            С уважением,
            Команда сайта
            """, userName != null ? ", " + userName : "");

        try {
            emailService.sendEmail(email, subject, text);
        } catch (Exception e) {
            log.error("Failed to send goodbye email to {}: {}", email, e.getMessage());
        }
    }

    public void sendCustomEmail(String email, String subject, String message) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        emailService.sendEmail(email, subject, message);
    }
}
