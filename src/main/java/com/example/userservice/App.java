package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class, args);
        } catch (Throwable t) {
            System.err.println("❌ Приложение завершилось с ошибкой:");
            t.printStackTrace(System.err);
            throw t;
        }
    }
}