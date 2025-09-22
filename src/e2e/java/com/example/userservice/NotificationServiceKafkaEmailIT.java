package com.example.notificationservice;

import com.example.notificationservice.model.UserEvent;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"user-events"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationServiceKafkaEmailIT {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private KafkaTemplate<String, UserEvent> kafkaTemplate;
    private GreenMail greenMail;

    @BeforeAll
    void setup() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
        greenMail.start();

        System.setProperty("spring.mail.host", "localhost");
        System.setProperty("spring.mail.port", "3025");
        System.setProperty("spring.mail.properties.mail.smtp.auth", "false");
        System.setProperty("spring.mail.properties.mail.smtp.starttls.enable", "false");

        Map<String, Object> producerProps = new HashMap<>(embeddedKafka.getConfigurationProperties());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
    }

    @AfterAll
    void tearDown() {
        greenMail.stop();
    }

    @Test
    void whenCreatedEvent_thenEmailSent() {
        kafkaTemplate.send("user-events", "alex@example.com", new UserEvent("CREATED", "alex@example.com", "Alex"));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertEquals(1, messages.length);
            assertEquals("Аккаунт создан", messages[0].getSubject());
        });
    }

    @Test
    void whenDeletedEvent_thenEmailSent() {
        kafkaTemplate.send("user-events", "alex@example.com", new UserEvent("DELETED", "alex@example.com", "Alex"));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertEquals(1, messages.length);
            assertEquals("Аккаунт удалён", messages[0].getSubject());
        });
    }
}