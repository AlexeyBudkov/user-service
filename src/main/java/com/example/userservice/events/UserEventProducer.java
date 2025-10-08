package com.example.userservice.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserEventProducer {

    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);
    private static final String CIRCUIT_NAME = "kafkaProducer";

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
        send(UserEventFactory.created(email, age));
    }

    public void sendDeleted(String email, Integer age) {
        send(UserEventFactory.deleted(email, age));
    }

    @CircuitBreaker(name = CIRCUIT_NAME, fallbackMethod = "sendFallback")
    private void send(UserEvent event) {
        try {
            log.info("📤 Sending event to Kafka: {}", objectMapper.writeValueAsString(event));
            kafkaTemplate.send(topic, event.email(), event).get();
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации UserEvent", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Ошибка при отправке события в Kafka", e);
            throw new RuntimeException(e);
        }
    }

    private void sendFallback(UserEvent event, Throwable t) {
        log.warn("⚠️ Kafka недоступна, событие не отправлено: {}. Причина: {}",
                event, t.getMessage());
    }
}
