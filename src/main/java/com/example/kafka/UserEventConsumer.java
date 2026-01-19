package com.example.kafka;

import com.example.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void consume(String message) {
        try {
            log.info("📥 RAW Kafka message: {}", message);

            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            String email = (String) event.get("email");
            String userName = (String) event.get("userName");

            log.info("✅ Parsed event: {}, email: {}, user: {}", eventType, email, userName);

            if ("USER_CREATED".equals(eventType)) {
                notificationService.sendWelcomeEmail(email, userName != null ? userName : "пользователь");
            } else if ("USER_DELETED".equals(eventType)) {
                notificationService.sendGoodbyeEmail(email, userName != null ? userName : "пользователь");
            }

        } catch (Exception e) {
            log.error("❌ Error processing message: {}", e.getMessage());
        }
    }
}
