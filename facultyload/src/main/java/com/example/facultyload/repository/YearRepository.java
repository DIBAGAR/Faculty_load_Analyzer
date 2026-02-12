package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.Year;
import java.util.List;

public interface YearRepository extends JpaRepository<Year, Integer> {
    List<Year> findByDepartmentDepartmentId(Integer departmentId);
}