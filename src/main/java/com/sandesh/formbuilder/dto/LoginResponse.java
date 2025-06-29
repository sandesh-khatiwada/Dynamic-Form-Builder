package com.sandesh.formbuilder.dto;

import com.sandesh.formbuilder.entity.Role;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class LoginResponse {

        private String email;
        private String token;
        private List<String> roles;
}
