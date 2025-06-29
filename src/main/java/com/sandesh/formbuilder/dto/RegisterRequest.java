package com.sandesh.formbuilder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {


    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message ="Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,20}$",
            message = "Password must be 8-20 characters, include uppercase, lowercase, number, and special character.")
    private String password;

    @NotBlank(message = "Role is required")
    private String role;
}
