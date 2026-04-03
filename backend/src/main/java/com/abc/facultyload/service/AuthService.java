package com.abc.facultyload.service;

import com.abc.facultyload.dto.request.LoginRequest;
import com.abc.facultyload.dto.response.AuthResponse;
import com.abc.facultyload.entity.User;
import com.abc.facultyload.repository.UserRepository;
import com.abc.facultyload.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserId(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmailOrRollNumber(loginRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .role(user.getRole().getName().name())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}

