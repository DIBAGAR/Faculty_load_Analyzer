package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.Users;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
}