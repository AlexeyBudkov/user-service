package com.example.userservice.exception;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ApiError {
    LocalDateTime timestamp = LocalDateTime.now();
    int status;
    String error;
    String message;
    String path;
}
