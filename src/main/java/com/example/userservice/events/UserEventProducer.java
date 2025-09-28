package com.example.userservice.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserEventProducer {

    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;
    private final ObjectMapper objectMapper;

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate,
                             @Value("${app.kafka.topic}") String topic,
                             ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    public void sendCreated(String email, Integer age) {
        UserEvent event = UserEventFactory.created(email, age);
        send(event);
    }

    public void sendDeleted(String email, Integer age) {
        UserEvent event = UserEventFactory.deleted(email, age);
        send(event);
    }

    private void send(UserEvent event) {
        try {
            log.info("📤 Sending event to Kafka: {}", objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации UserEvent", e);
        }
        kafkaTemplate.send(topic, event.email(), event);
    }
}
