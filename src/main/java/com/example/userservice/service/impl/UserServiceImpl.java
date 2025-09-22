package com.example.userservice.service.impl;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.events.UserEvent;
import com.example.userservice.events.UserEventProducer;
import com.example.userservice.exception.NotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository repo;
    private final UserEventProducer producer;

    public UserServiceImpl(UserRepository repo, UserEventProducer producer) {
        this.repo = repo;
        this.producer = producer;
    }

    @Override
    public UserResponse create(UserRequest request) {
        log.info(">>> create() called with: name={}, email={}, age={}",
                request != null ? request.getName() : null,
                request != null ? request.getEmail() : null,
                request != null ? request.getAge() : null);
        try {
            validateUser(request);

            if (repo.findByEmail(request.getEmail()).isPresent()) {
                log.warn("Попытка создать пользователя с уже существующим email: {}", request.getEmail());
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }

            User saved = repo.save(UserMapper.toEntity(request));
            log.info("Пользователь с ID {} создан", saved.getId());

            producer.send(UserEvent.created(saved.getEmail(), saved.getAge()));

            return UserMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при создании пользователя (DataIntegrityViolation): {}", e.getMostSpecificCause().getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании пользователя", e);
            throw e;
        }
    }

    @Override
    public UserResponse getById(Long id) {
        if (!isValidId(id)) {
            log.warn("Некорректный ID пользователя для поиска: {}", id);
            throw new NotFoundException("Некорректный ID");
        }
        return repo.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });
    }

    @Override
    public List<UserResponse> getAll() {
        List<User> users = repo.findAll();
        if (users.isEmpty()) {
            log.warn("Список пользователей пуст");
        } else {
            log.info("Найдено {} пользователей", users.size());
        }
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        validateUser(request);
        if (!isValidId(id)) {
            throw new IllegalArgumentException("ID пользователя обязателен для обновления");
        }

        User existing = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден для обновления", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });

        if (request.getEmail() != null && !request.getEmail().equals(existing.getEmail())) {
            if (repo.findByEmail(request.getEmail()).isPresent()) {
                log.warn("Попытка обновить пользователя на email, который уже занят: {}", request.getEmail());
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
        }

        UserMapper.updateEntity(existing, request);
        User updated = repo.save(existing);
        log.info("Пользователь с ID {} обновлён", id);
        return UserMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!isValidId(id)) {
            log.warn("Некорректный ID для удаления: {}", id);
            throw new IllegalArgumentException("Некорректный ID");
        }
        if (repo.existsById(id)) {
            User existing = repo.findById(id).orElseThrow();
            repo.deleteById(id);
            log.info("Пользователь с ID {} удалён", id);

            producer.send(UserEvent.deleted(existing.getEmail(), existing.getAge()));

        } else {
            log.warn("Пользователь с ID {} не найден для удаления", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public UserResponse getByEmail(String email) {
        if (email == null || email.isBlank()) {
            log.warn("Некорректный email для поиска: {}", email);
            throw new IllegalArgumentException("Некорректный email");
        }
        return repo.findByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Пользователь с email {} не найден", email);
                    return new NotFoundException("Пользователь с email=" + email + " не найден");
                });
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    private void validateUser(UserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (request.getName() == null || request.getName().isBlank() || request.getName().length() < 2) {
            throw new IllegalArgumentException("Имя должно содержать минимум 2 символа и не быть пустым");
        }
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Некорректный email");
        }
    }
}