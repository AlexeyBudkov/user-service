package com.example.userservice.events;

public record UserEvent(String operation, String email, Integer age) {

    public static UserEvent created(String email, Integer age) {
        return new UserEvent("CREATE", email, age);
    }

    public static UserEvent deleted(String email, Integer age) {
        return new UserEvent("DELETE", email, age);
    }
}