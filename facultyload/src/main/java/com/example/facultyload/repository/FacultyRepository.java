package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.Faculty;
import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Integer> {
    List<Faculty> findByDepartmentDepartmentId(Integer departmentId);
}