package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }

    @Test
    void createUser_Success() throws Exception {
        UserRequest req = new UserRequest("Alex", "alex@example.com", 30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Alex")))
                .andExpect(jsonPath("$.email", is("alex@example.com")))
                .andExpect(jsonPath("$.age", is(30)));
    }

    @Test
    void createUser_DuplicateEmail_Returns400_JSON() throws Exception {
        repo.save(new User(null, "Alex", "alex@example.com", 30));

        UserRequest req = new UserRequest("Another", "alex@example.com", 25);

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