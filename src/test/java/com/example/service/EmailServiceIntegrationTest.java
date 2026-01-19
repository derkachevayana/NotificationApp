package com.example.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, ports = 9092)
@DirtiesContext
class EmailServiceIntegrationTest {

    private static final int GREENMAIL_PORT = 3025;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(GREENMAIL_PORT, null, ServerSetup.PROTOCOL_SMTP)
    ).withConfiguration(
            GreenMailConfiguration.aConfig().withUser("test", "test")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");

        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> GREENMAIL_PORT);
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
    }

    @Autowired
    private EmailService emailService;

    @Test
    void shouldSendEmailSuccessfully() throws Exception {
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test email content";

        emailService.sendEmail(to, subject, text);

        Thread.sleep(1000);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        MimeMessage message = receivedMessages[0];
        assertThat(message.getSubject()).isEqualTo(subject);
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(to);
        assertThat(GreenMailUtil.getBody(message)).contains(text);
    }
}
