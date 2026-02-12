package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.Semester;
import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Integer> {
    List<Semester> findByYearYearId(Integer yearId);
}