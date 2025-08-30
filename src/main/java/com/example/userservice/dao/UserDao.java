package com.example.userservice.dao;

import com.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Long create(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void update(User user);

    void deleteById(Long id);
}
