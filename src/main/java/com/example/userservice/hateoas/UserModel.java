package com.example.userservice.hateoas;

import org.springframework.hateoas.RepresentationModel;

public class UserModel extends RepresentationModel<UserModel> {
    private Long id;
    private String name;
    private String email;
    private Integer age;

    public UserModel(Long id, String name, String email, Integer age) {
        this.id = id; this.name = name; this.email = email; this.age = age;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }
}
