package com.abc.facultyload.config;

import com.abc.facultyload.security.JwtAuthFilter;
import com.abc.facultyload.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> 
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            ))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/department/**").hasAnyRole("DEPARTMENT_ADMIN", "SUPER_ADMIN", "HOD", "TEMP_HOD", "FACULTY", "FACULTY_ADMIN", "COURSE_ADMIN", "VENUE_ADMIN")
                .requestMatchers("/api/faculty-admin/**").hasAnyRole("FACULTY_ADMIN", "SUPER_ADMIN")
                // HOD/TEMP_HOD need GET access for timetable dropdowns
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/course-admin/**").hasAnyRole("COURSE_ADMIN", "SUPER_ADMIN", "HOD", "TEMP_HOD", "FACULTY")
                .requestMatchers("/api/course-admin/**").hasAnyRole("COURSE_ADMIN", "SUPER_ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/venue-admin/**").hasAnyRole("VENUE_ADMIN", "SUPER_ADMIN", "HOD", "TEMP_HOD")
                .requestMatchers("/api/venue-admin/**").hasAnyRole("VENUE_ADMIN", "SUPER_ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/faculty-admin/faculties").hasAnyRole("FACULTY_ADMIN", "SUPER_ADMIN", "HOD", "TEMP_HOD")
                .requestMatchers("/api/hod/**").hasAnyRole("HOD", "TEMP_HOD", "SUPER_ADMIN", "FACULTY_ADMIN")
                .requestMatchers("/api/faculty/**").hasAnyRole("FACULTY", "HOD", "TEMP_HOD", "SUPER_ADMIN", "FACULTY_ADMIN")
                .requestMatchers("/api/notifications/**").authenticated()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
