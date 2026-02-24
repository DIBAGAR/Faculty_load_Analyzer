package com.example.facultyload.repository;

import com.example.facultyload.entity.MonthlyWorkloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MonthlyWorkloadHistoryRepository extends JpaRepository<MonthlyWorkloadHistory, Long> {
    Optional<MonthlyWorkloadHistory> findByFacultyIdAndMonthStartDate(Long facultyId, LocalDate monthStartDate);
    List<MonthlyWorkloadHistory> findByFacultyIdOrderByMonthStartDateDesc(Long facultyId);
}

