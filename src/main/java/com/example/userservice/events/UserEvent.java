package com.example.userservice.events;

public record UserEvent(String operation, String email, Integer age) {
}
