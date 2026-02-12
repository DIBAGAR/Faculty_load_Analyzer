package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.FacultyPerformance;
import java.util.List;

public interface FacultyPerformanceRepository extends JpaRepository<FacultyPerformance, Integer> {
    List<FacultyPerformance> findByFacultyFacultyId(Integer facultyId);
}