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
        System.out.println("=".repeat(60));
        System.out.println("✅ Notification Service запущен успешно!");
        System.out.println("=".repeat(60));
        System.out.println("📡 Зарегистрирован в Service Discovery (Eureka)");
        System.out.println("🔧 Конфигурация из Config Server");
        System.out.println("=".repeat(60));
        System.out.println("🔗 Доступ через Gateway: http://localhost:8090/api/notifications");
        System.out.println("📚 Swagger UI (Gateway): http://localhost:8090/swagger-ui.html");
        System.out.println("📧 SMTP: localhost:1025 (MailDev)");
        System.out.println("📨 Web UI: http://localhost:1080");
        System.out.println("📊 Kafka: localhost:9092");
        System.out.println("🎯 Топик: user-events");
        System.out.println("👥 Consumer Group: notification-group");
        System.out.println("=".repeat(60));
        System.out.println("🚀 Готов к приему событий из Kafka!");
        System.out.println("=".repeat(60));

    }
}