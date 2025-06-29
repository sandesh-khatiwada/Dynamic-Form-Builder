package com.sandesh.formbuilder.service;

import com.sandesh.formbuilder.dto.LoginRequest;
import com.sandesh.formbuilder.dto.LoginResponse;
import com.sandesh.formbuilder.dto.RegisterRequest;
import com.sandesh.formbuilder.dto.RegisterResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);
    RegisterResponse registerUser(RegisterRequest registerRequest);

}
