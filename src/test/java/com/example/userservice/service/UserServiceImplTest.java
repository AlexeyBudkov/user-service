package com.example.userservice.service;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.exception.NotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUser_Success() {
        UserRequest request = new UserRequest("Alex", "alex@example.com", 30);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.create(request);

        assertEquals("Alex", response.getName());
        assertEquals("alex@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getById_NotFound_Throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    void getAll_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(
                new User(1L, "Alex", "alex@example.com", 30),
                new User(2L, "Bob", "bob@example.com", 25)
        ));

        List<UserResponse> responses = userService.getAll();
        assertEquals(2, responses.size());
    }

    @Test
    void delete_ExistingUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_Throws() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.delete(1L));
    }
}