package com.example.kafka;

import com.example.dto.UserEvent;
import com.example.dto.UserEventType;
import com.example.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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

            UserEvent event = objectMapper.readValue(message, UserEvent.class);

            log.info("✅ Parsed event: {}, email: {}, user: {}",
                    event.getEventType(), event.getEmail(), event.getUserName());

            if (event.getEventType() == UserEventType.USER_CREATED) {
                String userName = event.getUserName() != null ? event.getUserName() : "пользователь";
                notificationService.sendWelcomeEmail(event.getEmail(), userName);
            } else if (event.getEventType() == UserEventType.USER_DELETED) {
                String userName = event.getUserName() != null ? event.getUserName() : "пользователь";
                notificationService.sendGoodbyeEmail(event.getEmail(), userName);
            } else {
                log.warn("⚠️ Unknown event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("❌ Error processing message: {}", e.getMessage(), e);
        }
    }
}
