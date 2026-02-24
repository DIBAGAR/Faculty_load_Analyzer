package com.example.facultyload.service.impl;

import com.example.facultyload.dto.auth.LoginResponse;
import com.example.facultyload.dto.auth.UserInfoResponse;
import com.example.facultyload.entity.Faculty;
import com.example.facultyload.entity.User;
import com.example.facultyload.repository.FacultyRepository;
import com.example.facultyload.repository.UserRepository;
import com.example.facultyload.security.JwtService;
import com.example.facultyload.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final JwtService jwtService;

    @Override
    public LoginResponse issueTokenForEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        String token = jwtService.generateToken(user.getEmail(), user.getRole().getName());

        Faculty faculty = facultyRepository.findByUserEmailIgnoreCase(user.getEmail()).orElse(null);
        Long facultyId = faculty != null ? faculty.getId() : null;
        Long departmentId = faculty != null ? faculty.getDepartment().getId() : null;

        return new LoginResponse(
                token,
                new UserInfoResponse(user.getId(), user.getEmail(), user.getRole().getName(), facultyId, departmentId)
        );
    }
}

