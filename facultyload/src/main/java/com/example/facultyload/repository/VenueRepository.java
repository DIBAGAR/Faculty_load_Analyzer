package com.example.facultyload.repository;

import com.example.facultyload.entity.Venue;
import com.example.facultyload.entity.VenueType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByDepartmentIdAndIsActiveTrue(Long departmentId);
    List<Venue> findByDepartmentIdAndTypeAndIsActiveTrue(Long departmentId, VenueType type);
    Optional<Venue> findByDepartmentIdAndCodeIgnoreCase(Long departmentId, String code);
}

