package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.entity.User;
import com.example.userservice.events.UserEventProducer;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerEdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserEventProducer userEventProducer;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }

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

        repo.save(new User(null, "Alex", "alex@example.com", 30));
        User existing2 = repo.save(new User(null, "Bob", "bob@example.com", 25));

        UserRequest req = new UserRequest("Bob Updated", "alex@example.com", 26);

        mockMvc.perform(put("/api/users/{id}", existing2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("уже существует")))
                .andExpect(jsonPath("$.path", is("/api/users/" + existing2.getId())));
    }
}