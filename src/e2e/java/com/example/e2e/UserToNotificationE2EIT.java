package com.example.e2e;

import com.example.userservice.dto.UserRequest;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                com.example.userservice.UserServiceApp.class,
                com.example.notificationservice.NotificationServiceApp.class
        }
)
@EmbeddedKafka(
        partitions = 1,
        topics = {"user-events"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserToNotificationE2EIT {

    @Autowired
    private TestRestTemplate restTemplate;

    private GreenMail greenMail;

    @BeforeAll
    void setup() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
        greenMail.start();
    }

    @AfterAll
    void tearDown() {
        greenMail.stop();
    }

    @Test
    void createUser_shouldTriggerCreatedAndDeletedEmail() {
        UserRequest request = new UserRequest();
        request.setName("Alex");
        request.setEmail("alex@example.com");

        Long userId = restTemplate.postForEntity("/users", request, Long.class).getBody();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertEquals(1, messages.length);
            assertEquals("Аккаунт создан", messages[0].getSubject());
        });

        greenMail.purgeEmailFromAllMailboxes();

        restTemplate.delete("/users/{id}", userId);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertEquals(1, messages.length);
            assertEquals("Аккаунт удалён", messages[0].getSubject());
        });
    }
}
