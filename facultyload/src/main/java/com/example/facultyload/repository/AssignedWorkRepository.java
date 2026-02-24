package com.example.facultyload.repository;

import com.example.facultyload.entity.AssignedWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssignedWorkRepository extends JpaRepository<AssignedWork, Long> {
    List<AssignedWork> findByFacultyIdAndWeekStartDateOrderByWorkDateAsc(Long facultyId, LocalDate weekStartDate);
    List<AssignedWork> findByFacultyIdOrderByWorkDateDesc(Long facultyId);
    boolean existsByFacultyIdAndWorkDateAndTimetableEntry_DayOfWeekAndTimetableEntry_HourNumber(
            Long facultyId,
            LocalDate workDate,
            java.time.DayOfWeek dayOfWeek,
            Integer hourNumber
    );

    @Query("""
            select aw from AssignedWork aw
            where aw.faculty.id = :facultyId
              and aw.weekStartDate >= :minWeekStart
            order by aw.weekStartDate desc, aw.workDate desc, aw.id desc
            """)
    List<AssignedWork> findRecentWeeks(
            @Param("facultyId") Long facultyId,
            @Param("minWeekStart") LocalDate minWeekStart
    );

    @Query("""
            select aw from AssignedWork aw
            where aw.faculty.id = :facultyId
              and aw.workDate between :fromDate and :toDate
              and (:hourNumber is null or aw.timetableEntry.hourNumber = :hourNumber)
            """)
    List<AssignedWork> findAssignmentsForLeaveWindow(
            @Param("facultyId") Long facultyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("hourNumber") Integer hourNumber
    );

    long deleteByFacultyIdAndWeekStartDateBefore(Long facultyId, LocalDate weekStartDate);
}

