package com.example.kafka;

import com.example.dto.UserEvent;
import com.example.dto.UserEventType;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        ports = 9092,
        topics = {"user-events"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092",
                "auto.create.topics.enable=true"
        }
)
@Slf4j
class NotificationKafkaIntegrationTest {

    private static final int GREENMAIL_PORT = 3025;

    private static GreenMail greenMail;

    @BeforeAll
    static void beforeAll() {
        greenMail = new GreenMail(new ServerSetup(GREENMAIL_PORT, null, ServerSetup.PROTOCOL_SMTP))
                .withConfiguration(GreenMailConfiguration.aConfig()
                        .withUser("test", "test")
                        .withDisabledAuthentication());
        greenMail.start();
    }

    @AfterAll
    static void afterAll() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> GREENMAIL_PORT);
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.connectiontimeout", () -> "5000");
        registry.add("spring.mail.properties.mail.smtp.timeout", () -> "5000");
        registry.add("spring.mail.properties.mail.smtp.writetimeout", () -> "5000");

        registry.add("spring.mail.default-encoding", () -> "UTF-8");
        registry.add("spring.mail.properties.mail.mime.charset", () -> "UTF-8");

        registry.add("spring.mail.properties.mail.debug", () -> "true");
        registry.add("logging.level.org.springframework.mail", () -> "DEBUG");
    }

    @BeforeEach
    void setUp() {
        try {
            greenMail.purgeEmailFromAllMailboxes();
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("Warning: Failed to purge mailboxes: " + e.getMessage());
        }
    }

    @Test
    void shouldSendWelcomeEmailWhenUserCreatedEventReceived() throws Exception {
        String email = "kafka-test@example.com";
        String userName = "Kafka User";
        long timestamp = System.currentTimeMillis();

        UserEvent event = new UserEvent(
                UserEventType.USER_CREATED,
                email,
                123L,
                userName,
                timestamp
        );

        String eventJson = objectMapper.writeValueAsString(event);

        log.info("Sending event to Kafka: {}", eventJson);

        kafkaTemplate.send("user-events", email, eventJson).get(5, TimeUnit.SECONDS);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(1);

            MimeMessage message = messages[0];
            assertThat(message.getSubject()).isEqualTo("Добро пожаловать!");
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo(email);

            String content = getMessageContent(message);
            assertThat(content).contains(userName);
        });
    }

    @Test
    void shouldSendGoodbyeEmailWhenUserDeletedEventReceived() throws Exception {
        String email = "delete-test@example.com";
        String userName = "Delete User";

        UserEvent event = new UserEvent(
                UserEventType.USER_DELETED,
                email,
                456L,
                userName,
                System.currentTimeMillis()
        );

        String eventJson = objectMapper.writeValueAsString(event);

        kafkaTemplate.send("user-events", email, eventJson).get(5, TimeUnit.SECONDS);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(1);

            MimeMessage message = messages[0];
            assertThat(message.getSubject()).isEqualTo("Ваш аккаунт удален");
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo(email);

            String content = getMessageContent(message);
            assertThat(content).contains(userName);
        });
    }

    @Test
    void shouldIgnoreUnknownEventTypes() throws Exception {

        String invalidEventJson = """
                {
                    "eventType": "INVALID_TYPE",
                    "email": "unknown@example.com",
                    "timestamp": %d
                }
                """.formatted(System.currentTimeMillis());

        kafkaTemplate.send("user-events", "unknown@example.com", invalidEventJson);

        Thread.sleep(3000);
        assertThat(greenMail.getReceivedMessages()).isEmpty();
    }

    private String getMessageContent(MimeMessage message) throws Exception {
        try {
            Object content = message.getContent();

            if (content instanceof String) {
                String text = (String) content;
                if (isBase64(text.trim())) {
                    return decodeBase64(text.trim());
                }
                return text;

            } else if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                StringBuilder contentBuilder = new StringBuilder();

                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (bodyPart.isMimeType("text/plain")) {
                        Object partContent = bodyPart.getContent();
                        if (partContent instanceof String) {
                            String text = (String) partContent;
                            if (isBase64(text.trim())) {
                                contentBuilder.append(decodeBase64(text.trim()));
                            } else {
                                contentBuilder.append(text);
                            }
                        }
                    }
                }
                return contentBuilder.toString();
            }

        } catch (Exception e) {
            System.err.println("Error getting message content: " + e.getMessage());
        }

        String content = GreenMailUtil.getBody(message);
        if (isBase64(content.trim())) {
            return decodeBase64(content.trim());
        }
        return content;
    }

    private boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        String trimmed = str.trim();
        return trimmed.matches("^[A-Za-z0-9+/]+={0,2}$");
    }

    private String decodeBase64(String base64) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to decode base64: " + e.getMessage());
            return base64;
        }
    }
}