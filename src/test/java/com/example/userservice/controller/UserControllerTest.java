package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.events.UserEventProducer;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserEventProducer userEventProducer;

    @Test
    @DisplayName("POST /api/users — успешное создание")
    void createUser_Success() throws Exception {
        UserRequest req = new UserRequest("Alex", "alex@example.com", 30);

        when(userService.create(any(UserRequest.class))).thenReturn(
                UserResponse.builder()
                        .id(1L)
                        .name("Alex")
                        .email("alex@example.com")
                        .age(30)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Alex")))
                .andExpect(jsonPath("$.email", is("alex@example.com")))
                .andExpect(jsonPath("$.age", is(30)));
    }

    @Test
    @DisplayName("POST /api/users — дубликат email, ожидаем 400")
    void createUser_DuplicateEmail_Returns400_JSON() throws Exception {
        UserRequest req = new UserRequest("Another", "alex@example.com", 25);

        when(userService.create(any(UserRequest.class)))
                .thenThrow(new IllegalArgumentException("Email уже существует"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("уже существует")))
                .andExpect(jsonPath("$.path", is("/api/users")));
    }
}
