package com.example.facultyload.service;

import com.example.facultyload.dto.auth.LoginResponse;

public interface AuthService {
    LoginResponse issueTokenForEmail(String email);
}

