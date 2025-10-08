package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerEdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserEventProducer userEventProducer;

    @Test
    @DisplayName("POST /api/users — имя слишком короткое, ожидаем 400")
    void createUser_InvalidName_JSON() throws Exception {
        UserRequest req = new UserRequest("A", "alex@example.com", 30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Имя должно содержать минимум 2 символа")))
                .andExpect(jsonPath("$.path", is("/api/users")));
    }

    @Test
    @DisplayName("GET /api/users/{id} — пользователь не найден, ожидаем 404")
    void getById_NotFound_JSON() throws Exception {
        when(userService.getById(999L)).thenThrow(new RuntimeException("Пользователь не найден"));

        mockMvc.perform(get("/api/users/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("не найден")))
                .andExpect(jsonPath("$.path", is("/api/users/999")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} — email уже существует, ожидаем 400")
    void updateUser_EmailAlreadyExists_JSON() throws Exception {
        UserRequest req = new UserRequest("Bob Updated", "alex@example.com", 26);

        when(userService.update(any(Long.class), any(UserRequest.class)))
                .thenThrow(new IllegalArgumentException("Email уже существует"));

        mockMvc.perform(put("/api/users/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("уже существует")))
                .andExpect(jsonPath("$.path", is("/api/users/2")));
    }
}
