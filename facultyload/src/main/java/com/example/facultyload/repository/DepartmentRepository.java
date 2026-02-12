package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    // Custom query example:
    // List<Department> findByDepartmentName(String name);
}