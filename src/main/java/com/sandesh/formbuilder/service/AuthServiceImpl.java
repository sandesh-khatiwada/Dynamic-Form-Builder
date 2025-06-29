package com.sandesh.formbuilder.service;

import com.sandesh.formbuilder.dto.LoginRequest;
import com.sandesh.formbuilder.dto.LoginResponse;
import com.sandesh.formbuilder.dto.RegisterRequest;
import com.sandesh.formbuilder.dto.RegisterResponse;
import com.sandesh.formbuilder.entity.User;
import com.sandesh.formbuilder.entity.Role;
import com.sandesh.formbuilder.exception.UserAlreadyExistsException;
import com.sandesh.formbuilder.repository.RoleRepository;
import com.sandesh.formbuilder.repository.UserRepository;
import com.sandesh.formbuilder.repository.UserRepositoryCustom;
import com.sandesh.formbuilder.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserRepositoryCustom userRepositoryCustom;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        initializeRoles();
        if(Objects.equals(loginRequest.getEmail(), "") || loginRequest.getEmail()==null){
            throw new IllegalArgumentException("Email is required");
        }

        if(Objects.equals(loginRequest.getPassword(), "") || loginRequest.getPassword()==null){
            throw new IllegalArgumentException("Password is required");

        }


        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect username or password");
        }


        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new UsernameNotFoundException("User not found with email"+loginRequest.getEmail()));

        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setEmail(user.getEmail());
        loginResponse.setToken(jwt);

        List<String> roles = new ArrayList<>();
        user.getRoles().forEach((role -> roles.add(role.getName())));
        loginResponse.setRoles(roles);

        return loginResponse;
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest registerRequest) {

        initializeRoles();

        if(registerRequest.getEmail()==null || registerRequest.getEmail().isEmpty()){
            throw new IllegalArgumentException("Invalid email: " + registerRequest.getEmail());
        }

        if(registerRequest.getUsername()==null || registerRequest.getUsername().isEmpty()){
            throw new IllegalArgumentException("Invalid username: "+registerRequest.getUsername());
        }

        if(registerRequest.getPassword()==null || registerRequest.getPassword().isEmpty()){
            throw new IllegalArgumentException("Invalid password: ");
        }

        if (!registerRequest.getRole().equals("USER") && !registerRequest.getRole().equals("ADMIN")) {
            throw new IllegalArgumentException("Invalid role: " + registerRequest.getRole());
        }

        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            throw new UserAlreadyExistsException("The email is already registered");
        }


        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        userRepositoryCustom.insertUserWithRoles(registerRequest.getEmail(), registerRequest.getUsername(), hashedPassword, registerRequest.getRole());

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setEmail(registerRequest.getEmail());
        registerResponse.setUsername(registerRequest.getUsername());
        registerResponse.setRole(registerRequest.getRole());

        return registerResponse;
    }

    public void initializeRoles() {
        if (roleRepository.findByName("USER").isEmpty()) {
            roleRepository.save(new Role(null, "USER"));
        }
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ADMIN"));
        }
    }


}
