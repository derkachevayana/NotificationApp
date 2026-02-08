package com.example.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceIntegrationTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendWelcomeEmail_Success() {
        notificationService.sendWelcomeEmail("test@example.com", "John");

        verify(emailService).sendEmail(
                eq("test@example.com"),
                eq("Добро пожаловать!"),
                anyString()
        );
    }

    @Test
    void sendCustomEmail_Success() {
        notificationService.sendCustomEmail("test@example.com", "Subject", "Message");

        verify(emailService).sendEmail("test@example.com", "Subject", "Message");
    }
}