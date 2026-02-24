package com.example.facultyload.dto.auth;

public record LoginResponse(
        String token,
        UserInfoResponse user
) {
}

