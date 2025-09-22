package com.example.userservice;

import com.example.userservice.entity.User;
import com.example.userservice.events.UserEvent;
import com.example.userservice.events.UserEventProducer;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableKafka
@EmbeddedKafka(
        partitions = 1,
        topics = {"user-events"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
class UserServiceKafkaDeleteIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockBean
    private UserEventProducer userEventProducer;

    private Consumer<String, UserEvent> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("deleteTestGroup", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonDeserializer.class);
        consumerProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.VALUE_DEFAULT_TYPE, UserEvent.class);

        consumer = new DefaultKafkaConsumerFactory<String, UserEvent>(consumerProps).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "user-events");
    }

    @Test
    void whenDeleteUser_thenKafkaEventProduced() {
        User user = new User();
        user.setName("Alex");
        user.setEmail("alex@example.com");
        userRepository.save(user);

        userService.delete(user.getId());

        ConsumerRecords<String, UserEvent> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertThat(records.count()).isGreaterThan(0);

        UserEvent event = records.iterator().next().value();
        assertThat(event.operation()).isEqualTo("DELETED");
        assertThat(event.email()).isEqualTo("alex@example.com");
    }
}
