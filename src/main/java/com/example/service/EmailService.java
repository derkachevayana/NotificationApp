package com.example.service;

import com.example.exception.EmailSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        if (to == null || to.isBlank()) {
            log.error("Cannot send email: recipient is empty");
            throw new IllegalArgumentException("Recipient email cannot be empty");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@example.com");

            mailSender.send(message);
            log.info("Email sent successfully to: {}, subject: {}", to, subject);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send email to " + to, e);
        }
    }
}

