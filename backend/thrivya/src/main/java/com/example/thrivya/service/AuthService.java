package com.example.thrivya.service;

import com.example.thrivya.dto.request.LoginRequest;
import com.example.thrivya.dto.request.RegisterRequest;
import com.example.thrivya.dto.response.AuthResponse;
import com.example.thrivya.model.User;
import com.example.thrivya.repository.UserRepository;
import com.example.thrivya.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse registerUser(RegisterRequest registerRequest){
        String email = registerRequest.getEmail().trim().toLowerCase();

        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();

        user.setUserName(registerRequest.getUsername().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());

        User savedUser = userRepository.save(user);

        UserDetails userDetails=org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getEmail())
                .password(savedUser.getPassword())
                .roles(savedUser.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public AuthResponse loginUser(LoginRequest loginRequest){
        String email = loginRequest.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        loginRequest.getPassword()
                )
        );

        User user=userRepository.findByEmail(email)
                .orElseThrow(()->
                        new IllegalArgumentException("User account was not found"));

        UserDetails userDetails=org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
