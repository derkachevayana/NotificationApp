package com.example;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
public class TestKafkaConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        return org.mockito.Mockito.mock(KafkaTemplate.class);
    }
}
