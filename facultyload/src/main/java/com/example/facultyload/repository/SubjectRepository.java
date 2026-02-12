package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, String> {
    // JpaRepository gives findAll() and save() automatically
}