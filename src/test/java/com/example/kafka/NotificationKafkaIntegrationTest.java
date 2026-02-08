package com.example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(
        partitions = 1,
        topics = {"test-topic"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093",
                "port=9093"
        }
)
class NotificationKafkaIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testKafkaProducerConsumer(EmbeddedKafkaBroker embeddedKafka) throws Exception {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                embeddedKafka.getBrokersAsString());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        Producer<String, String> producer =
                new org.apache.kafka.clients.producer.KafkaProducer<>(producerProps);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                embeddedKafka.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        Consumer<String, String> consumer =
                new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps);

        try {
            consumer.subscribe(java.util.Collections.singletonList("test-topic"));

            String testMessage = "{\"eventType\":\"TEST\",\"email\":\"test@example.com\"}";
            producer.send(new ProducerRecord<>("test-topic", "key", testMessage)).get();

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            assertThat(records.count()).isEqualTo(1);
            assertThat(records.iterator().next().value()).isEqualTo(testMessage);

        } finally {
            producer.close();
            consumer.close();
        }
    }
}