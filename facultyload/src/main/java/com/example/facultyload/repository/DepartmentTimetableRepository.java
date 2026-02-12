package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.DepartmentTimetable;
import java.util.List;

public interface DepartmentTimetableRepository extends JpaRepository<DepartmentTimetable, Integer> {

    // Custom query to find all timetables by department id
    List<DepartmentTimetable> findByDepartmentDepartmentId(Integer departmentId);
}