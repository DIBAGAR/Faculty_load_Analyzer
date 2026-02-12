package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.FacultyAvailability;
import java.util.List;

public interface FacultyAvailabilityRepository extends JpaRepository<FacultyAvailability, Integer> {
    List<FacultyAvailability> findByFacultyFacultyId(Integer facultyId);
}