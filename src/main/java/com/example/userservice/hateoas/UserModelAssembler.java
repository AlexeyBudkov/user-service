package com.example.userservice.hateoas;

import com.example.userservice.controller.UserController;
import com.example.userservice.dto.UserResponse;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserResponse, UserModel> {

    @Override
    public UserModel toModel(UserResponse resp) {
        UserModel model = new UserModel(resp.getId(), resp.getName(), resp.getEmail(), resp.getAge());
        model.add(linkTo(methodOn(UserController.class).getById(resp.getId())).withSelfRel());
        model.add(linkTo(methodOn(UserController.class).getAll()).withRel("users"));
        model.add(linkTo(methodOn(UserController.class).getByEmail(resp.getEmail())).withRel("by-email"));
        return model;
    }
}
