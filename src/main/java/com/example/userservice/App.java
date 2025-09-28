package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class, args);
        } catch (Throwable t) {
            log.error("❌ Приложение завершилось с ошибкой", t);
            throw t;
        }
    }
}
