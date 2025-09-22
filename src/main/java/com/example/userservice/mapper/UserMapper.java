package com.example.userservice.mapper;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;

public class UserMapper {

    public static User toEntity(UserRequest req) {
        return new User(req.getName(), req.getEmail(), req.getAge());
    }

    public static UserResponse toResponse(User entity) {
        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getAge(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static void updateEntity(User entity, UserRequest req) {
        if (req.getName() != null) {
            entity.setName(req.getName());
        }
        if (req.getEmail() != null) {
            entity.setEmail(req.getEmail());
        }
        if (req.getAge() != null) {
            entity.setAge(req.getAge());
        }
    }
}