package com.example.facultyload.repository;

import com.example.facultyload.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByTimetableId(Long timetableId);

    @Query("""
            select te from TimetableEntry te
            where te.isActive = true
              and te.departmentId = :departmentId
              and te.yearOfStudy = :yearOfStudy
              and te.section = :section
              and te.semester = :semester
              and te.dayOfWeek = :dayOfWeek
              and te.hourNumber = :hourNumber
            """)
    List<TimetableEntry> findActiveSectionSlot(
            @Param("departmentId") Long departmentId,
            @Param("yearOfStudy") Integer yearOfStudy,
            @Param("section") String section,
            @Param("semester") Integer semester,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("hourNumber") Integer hourNumber
    );
}

