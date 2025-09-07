package com.example.userservice.controller;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import com.example.userservice.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

public class UserController {

    private final UserService userService;

    public UserController() {
        UserDao userDao = new UserDaoImpl(); // твоя реализация DAO
        this.userService = new UserServiceImpl(userDao);
    }

    public Optional<User> getUser(Long id) {
        return userService.getUserById(id);
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public User createUser(User user) {
        return userService.createUser(user);
    }

    public boolean deleteUser(Long id) {
        return userService.deleteUser(id);
    }
}