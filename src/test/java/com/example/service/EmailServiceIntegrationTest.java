package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceIntegrationTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
    }

    @Test
    void sendEmail_Success() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test message";

        emailService.sendEmail(to, subject, text);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_ThrowsExceptionWhenEmailIsEmpty() {
        assertThatThrownBy(() ->
                emailService.sendEmail("", "Subject", "Text"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recipient email cannot be empty");
    }
}