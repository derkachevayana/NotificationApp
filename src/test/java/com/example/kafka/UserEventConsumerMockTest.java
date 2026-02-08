package com.example.kafka;

import com.example.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerMockTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserEventConsumer userEventConsumer;

    @Test
    void shouldProcessUserCreatedEvent() throws Exception {
        String eventJson = """
            {
                "eventType": "USER_CREATED",
                "email": "test@example.com",
                "userId": 123,
                "userName": "John Doe",
                "timestamp": 1234567890
            }
            """;

        UserEvent event = new UserEvent(
                UserEventType.USER_CREATED,
                "test@example.com",
                123L,
                "John Doe",
                1234567890L
        );

        when(objectMapper.readValue(eventJson, UserEvent.class)).thenReturn(event);

        userEventConsumer.consume(eventJson);

        verify(notificationService).sendWelcomeEmail("test@example.com", "John Doe");
    }

    @Test
    void shouldProcessUserDeletedEvent() throws Exception {
        String eventJson = """
            {
                "eventType": "USER_DELETED",
                "email": "delete@example.com",
                "userId": 456,
                "userName": "Jane Smith",
                "timestamp": 1234567890
            }
            """;

        UserEvent event = new UserEvent(
                UserEventType.USER_DELETED,
                "delete@example.com",
                456L,
                "Jane Smith",
                1234567890L
        );

        when(objectMapper.readValue(eventJson, UserEvent.class)).thenReturn(event);

        userEventConsumer.consume(eventJson);

        verify(notificationService).sendGoodbyeEmail("delete@example.com", "Jane Smith");
    }
}