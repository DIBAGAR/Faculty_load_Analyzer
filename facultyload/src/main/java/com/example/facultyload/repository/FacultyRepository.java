package com.example.facultyload.repository;

import com.example.facultyload.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findByDepartmentIdAndIsActiveTrue(Long departmentId);
    Optional<Faculty> findByUserEmailIgnoreCase(String email);

    @Query("""
            select f from Faculty f
            where f.department.id = :departmentId
              and f.isActive = true
            order by f.monthlyAssignedHours asc, f.totalAssignedHours asc, f.id asc
            """)
    List<Faculty> findActiveByDepartmentOrderByLoad(@Param("departmentId") Long departmentId);
}

