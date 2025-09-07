package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(User user) {
        validateUser(user);
        Long id = userDao.create(user);
        return userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Ошибка при создании пользователя"));
    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (!isValidId(id)) {
            log.warn("Некорректный ID пользователя для поиска: {}", id);
            return Optional.empty();
        }
        Optional<User> user = userDao.findById(id);
        logLookupResult(id, user);
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            log.warn("Некорректный email для поиска: {}", email);
            return Optional.empty();
        }
        Optional<User> user = userDao.findByEmail(email);
        if (user.isPresent()) {
            log.info("Пользователь с email {} найден", email);
        } else {
            log.warn("Пользователь с email {} не найден", email);
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userDao.findAll();
        if (users.isEmpty()) {
            log.warn("Список пользователей пуст");
        } else {
            log.info("Найдено {} пользователей", users.size());
        }
        return users;
    }

    @Override
    public User updateUser(User user) {
        validateUser(user);
        if (user.getId() == null || user.getId() <= 0) {
            throw new IllegalArgumentException("ID пользователя обязателен для обновления");
        }
        userDao.update(user);
        log.info("Пользователь с ID {} обновлён", user.getId());
        return user;
    }

    @Override
    public boolean deleteUser(Long id) {
        if (!isValidId(id)) {
            log.warn("Некорректный ID для удаления: {}", id);
            return false;
        }
        Optional<User> existing = userDao.findById(id);
        if (existing.isPresent()) {
            userDao.deleteById(id);
            log.info("Пользователь с ID {} удалён", id);
            return true;
        } else {
            log.warn("Пользователь с ID {} не найден для удаления", id);
            return false;
        }
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    private void logLookupResult(Long id, Optional<User> user) {
        if (user.isPresent()) {
            log.info("Пользователь с ID {} найден", id);
        } else {
            log.warn("Пользователь с ID {} не найден", id);
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (user.getName() == null || user.getName().isBlank() || user.getName().length() < 2) {
            throw new IllegalArgumentException("Имя должно содержать минимум 2 символа и не быть пустым");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Некорректный email");
        }
    }
}