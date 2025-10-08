package com.example.userservice.service;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse create(UserRequest request);
    UserResponse getById(Long id);
    List<UserResponse> getAll();
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
    UserResponse getByEmail(String email);
}