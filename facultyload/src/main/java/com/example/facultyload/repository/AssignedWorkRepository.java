package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.AssignedWork;
import java.util.List;

public interface AssignedWorkRepository extends JpaRepository<AssignedWork, Integer> {
    List<AssignedWork> findByFacultyFacultyId(Integer facultyId);
    List<AssignedWork> findByWeekStartDateBetween(java.time.LocalDate start, java.time.LocalDate end);
}