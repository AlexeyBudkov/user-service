package com.example.userservice.service;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.events.UserEventProducer;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserEventProducer userEventProducer;

    @Autowired
    private UserServiceImpl userService;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void createAndGetUser() {
        UserRequest request = new UserRequest("Alex", "alex@example.com", 30);

        UserResponse created = userService.create(request);

        assertNotNull(created.getId());
        assertEquals("Alex", created.getName());

        UserResponse fetched = userService.getById(created.getId());
        assertEquals("alex@example.com", fetched.getEmail());
    }
}