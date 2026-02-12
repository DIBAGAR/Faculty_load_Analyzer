package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.ClassEntity;
import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {
    List<ClassEntity> findBySemesterSemesterId(Integer semesterId);
}