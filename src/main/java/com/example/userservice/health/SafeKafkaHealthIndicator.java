package com.example.userservice.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class SafeKafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    public SafeKafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public Health health() {
        Properties props = new Properties();
        props.putAll(kafkaAdmin.getConfigurationProperties());
        props.put("request.timeout.ms", "2000");

        try (AdminClient client = AdminClient.create(props)) {
            ListTopicsResult topics = client.listTopics();
            topics.names().get(2, TimeUnit.SECONDS);
            return Health.up().withDetail("kafka", "Broker доступен").build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("kafka", "Broker недоступен")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
