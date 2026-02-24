package com.example.facultyload.repository;

import com.example.facultyload.entity.Role;
import com.example.facultyload.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

