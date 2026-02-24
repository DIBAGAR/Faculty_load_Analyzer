package com.example.facultyload.repository;

import com.example.facultyload.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    Optional<Timetable> findByDepartmentIdAndYearOfStudyAndSectionAndIsActiveTrue(Long departmentId, Integer yearOfStudy, String section);
    List<Timetable> findByDepartmentIdAndYearOfStudyAndSectionOrderByVersionNoDesc(Long departmentId, Integer yearOfStudy, String section);
}

