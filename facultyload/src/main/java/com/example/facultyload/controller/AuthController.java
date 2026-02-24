package com.example.facultyload.controller;

import com.example.facultyload.dto.auth.LoginRequest;
import com.example.facultyload.dto.auth.LoginResponse;
import com.example.facultyload.dto.auth.UserInfoResponse;
import com.example.facultyload.entity.Faculty;
import com.example.facultyload.entity.User;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.repository.FacultyRepository;
import com.example.facultyload.security.CustomUserDetails;
import com.example.facultyload.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final FacultyRepository facultyRepository;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Invalid email or password");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, httpRequest, httpResponse);

        return authService.issueTokenForEmail(authentication.getName());
    }

    @GetMapping("/me")
    public UserInfoResponse me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("Not authenticated");
        }

        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        Faculty faculty = facultyRepository.findByUserEmailIgnoreCase(user.getEmail()).orElse(null);
        Long facultyId = faculty != null ? faculty.getId() : null;
        Long departmentId = faculty != null ? faculty.getDepartment().getId() : null;
        return new UserInfoResponse(user.getId(), user.getEmail(), user.getRole().getName(), facultyId, departmentId);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        request.getSession(false);
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}

