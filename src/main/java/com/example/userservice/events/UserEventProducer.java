package com.example.userservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate,
                             @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(UserEvent event) {
        try {
            System.out.println("📤 Sending event to Kafka: " + objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            e.printStackTrace();
        }
        kafkaTemplate.send(topic, event.email(), event);
    }
}