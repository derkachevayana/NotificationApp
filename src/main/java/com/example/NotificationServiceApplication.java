package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
        log.info("""
            ==========================================
            🚀 Notification Service запущен успешно!
            ==========================================
            🔗 Local: http://localhost:8082
            📚 Swagger UI:   http://localhost:8082/swagger-ui.html
            📧 SMTP: localhost:1025 (MailDev)
            📨 Web UI: http://localhost:1080
            📊 Kafka: localhost:9092
            🎯 Топик: user-events
            👥 Consumer Group: notification-group
            ==========================================
            Готов к приему событий из Kafka!
            ==========================================
            """);
    }
}