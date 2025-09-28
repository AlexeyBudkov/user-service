package com.example.userservice.events;

public final class UserEventFactory {

    private UserEventFactory() {
    }

    public static UserEvent created(String email, Integer age) {
        return new UserEvent("CREATE", email, age);
    }

    public static UserEvent deleted(String email, Integer age) {
        return new UserEvent("DELETE", email, age);
    }
}
