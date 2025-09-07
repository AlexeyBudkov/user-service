package com.example.userservice.service;

import com.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    boolean deleteUser(Long id);
}