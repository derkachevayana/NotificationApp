package com.example;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import com.example.kafka.UserEventConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestNotificationConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        return org.mockito.Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    @Primary
    public UserEventConsumer userEventConsumer() {
        return org.mockito.Mockito.mock(UserEventConsumer.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
