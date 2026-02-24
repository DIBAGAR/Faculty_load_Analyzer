package com.example.facultyload.repository;

import com.example.facultyload.entity.LeaveRequest;
import com.example.facultyload.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("""
            select lr from LeaveRequest lr
            where lr.faculty.id = :facultyId
              and lr.status = :status
              and :date between lr.fromDate and lr.toDate
              and (lr.hourNumber is null or lr.hourNumber = :hourNumber)
            """)
    List<LeaveRequest> findApprovedLeaveCovering(
            @Param("facultyId") Long facultyId,
            @Param("status") LeaveStatus status,
            @Param("date") LocalDate date,
            @Param("hourNumber") Integer hourNumber
    );

    List<LeaveRequest> findByFacultyIdOrderByCreatedAtDesc(Long facultyId);
}

