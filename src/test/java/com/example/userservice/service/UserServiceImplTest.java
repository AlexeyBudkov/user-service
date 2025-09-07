package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Alex", "alex@example.com", 25);
        testUser.setEmail("alex@example.com");
    }

    @Test
    void createUser_ValidData_ReturnsUser() {
        when(userDao.create(any(User.class))).thenReturn(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        User created = userService.createUser(testUser);

        assertNotNull(created);
        assertEquals("Alex", created.getName());
        verify(userDao).create(testUser);
        verify(userDao).findById(1L);
    }

    @Test
    void createUser_InvalidEmail_ThrowsException() {
        testUser.setEmail("invalid-email");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(testUser));

        assertEquals("Некорректный email", ex.getMessage());
        verify(userDao, never()).create(any());
    }

    @Test
    void getUserById_UserExists_ReturnsUser() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("Alex", result.get().getName());
        verify(userDao).findById(1L);
    }

    @Test
    void getUserById_UserNotFound_ReturnsEmpty() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(99L);

        assertTrue(result.isEmpty());
        verify(userDao).findById(99L);
    }

    @Test
    void deleteUser_UserExists_ReturnsTrue() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        boolean deleted = userService.deleteUser(1L);

        assertTrue(deleted);
        verify(userDao).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound_ReturnsFalse() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        boolean deleted = userService.deleteUser(99L);

        assertFalse(deleted);
        verify(userDao, never()).deleteById(anyLong());
    }

    @Test
    void getAllUsers_ReturnsList() {
        when(userDao.findAll()).thenReturn(List.of(testUser));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("Alex", users.get(0).getName());
        verify(userDao).findAll();
    }
}