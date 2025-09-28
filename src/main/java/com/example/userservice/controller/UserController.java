package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.hateoas.UserModel;
import com.example.userservice.hateoas.UserModelAssembler;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User management API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;
    private final UserModelAssembler assembler;

    public UserController(UserService service, UserModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel create(@Valid @RequestBody UserRequest request) {
        UserResponse response = service.create(request);
        return assembler.toModel(response);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public UserModel getById(@PathVariable Long id) {
        return assembler.toModel(service.getById(id));
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public CollectionModel<UserModel> getAll() {
        return assembler.toCollectionModel(service.getAll());
    }

    @Operation(summary = "Update user by ID")
    @PutMapping("/{id}")
    public UserModel update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return assembler.toModel(service.update(id, request));
    }

    @Operation(summary = "Delete user by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @Operation(summary = "Get user by email")
    @GetMapping("/by-email")
    public UserModel getByEmail(@RequestParam String email) {
        return assembler.toModel(service.getByEmail(email));
    }
}
