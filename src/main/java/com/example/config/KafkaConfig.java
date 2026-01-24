package com.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@OpenAPIDefinition(
        info = @Info(
                title = "Notification Service API",
                version = "1.0",
                description = "Микросервис для отправки email уведомлений"
        )
)
public class KafkaConfig {
}
