package com.sandesh.formbuilder.controller;

import com.sandesh.formbuilder.dto.LoginRequest;
import com.sandesh.formbuilder.dto.LoginResponse;
import com.sandesh.formbuilder.dto.RegisterRequest;
import com.sandesh.formbuilder.dto.RegisterResponse;
import com.sandesh.formbuilder.service.AuthService;
import com.sandesh.formbuilder.util.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/auth/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authService.login(loginRequest);

        APIResponse<LoginResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Login Successful",
                loginResponse
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }


    @PostMapping("/auth/register")
    public ResponseEntity<APIResponse<RegisterResponse>> register( @Valid @RequestBody RegisterRequest registerRequest) {


        RegisterResponse registerResponse = authService.registerUser(registerRequest);

        APIResponse<RegisterResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Registration Successful",
                registerResponse
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);


    }
}
