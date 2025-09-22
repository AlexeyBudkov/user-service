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
@EmbeddedKafka(partitions = 1, topics = {"user-events"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserToNotificationE2EIT {

    @Autowired
    private TestRestTemplate restTemplate;

    private GreenMail greenMail;

    @BeforeAll
    void setup() {
        // Запускаем тестовый SMTP
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
        greenMail.start();

        System.setProperty("spring.mail.host", "localhost");
        System.setProperty("spring.mail.port", "3025");
        System.setProperty("spring.mail.properties.mail.smtp.auth", "false");
        System.setProperty("spring.mail.properties.mail.smtp.starttls.enable", "false");
    }

    @AfterAll
    void tearDown() {
        greenMail.stop();
    }

    @Test
    void createUser_shouldTriggerCreatedEmail() {
        // 1. Создаём пользователя
        UserRequest request = new UserRequest();
        request.setName("Alex");
        request.setEmail("alex@example.com");

        Long userId = restTemplate.postForEntity("/users", request, Long.class).getBody();

        // 2. Ждём письмо
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertEquals(1, messages.length);
            assertEquals("Аккаунт создан", messages[0].getSubject());
        });

        // 3. Очищаем почтовый ящик перед следующим тестом
        greenMail.purgeEmailFromAllMailboxes();

        // 4. Удаляем пользователя
        restTemplate.delete("/users/{id}", userId);

        // 5. Ждём письмо об удалении
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertEquals(1, messages.length);
            assertEquals("Аккаунт удалён", messages[0].getSubject());
        });
    }
}
