package com.sandesh.formbuilder.dto;


import lombok.Data;

@Data
public class RegisterResponse {
    private String username;
    private String email;
    private String role;
}
