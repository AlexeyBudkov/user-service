package com.example.userservice.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component("mail")
public class SafeMailHealthIndicator implements HealthIndicator {

    private final JavaMailSenderImpl mailSender;

    public SafeMailHealthIndicator(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Health health() {
        try {
            mailSender.testConnection();
            return Health.up()
                    .withDetail("mail", "SMTP доступен")
                    .withDetail("host", mailSender.getHost())
                    .withDetail("port", mailSender.getPort())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("mail", "SMTP недоступен")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}