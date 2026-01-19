package com.example.controller;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, ports = 9092)
@DirtiesContext
class NotificationControllerIntegrationTest {

    private static final int GREENMAIL_PORT = 3025;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(GREENMAIL_PORT, null, ServerSetup.PROTOCOL_SMTP)
    ).withConfiguration(
            GreenMailConfiguration.aConfig().withUser("test", "test")
    );

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");

        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> GREENMAIL_PORT);
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
    }

    @Test
    void shouldSendCustomEmailViaApi() throws Exception {
        String requestBody = """
            {
                "email": "api-test@example.com",
                "subject": "Test Subject",
                "message": "Test message from API"
            }
            """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully to api-test@example.com"));

        Thread.sleep(1000);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);

        MimeMessage message = messages[0];
        assertThat(message.getSubject()).isEqualTo("Test Subject");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("api-test@example.com");
        assertThat(GreenMailUtil.getBody(message)).contains("Test message from API");
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        String requestBody = """
            {
                "email": "invalid-email",
                "subject": "Test",
                "message": "Test"
            }
            """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
