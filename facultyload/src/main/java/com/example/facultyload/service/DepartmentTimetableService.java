package com.example.facultyload.service;

import com.example.facultyload.entity.DepartmentTimetable;
import com.example.facultyload.repository.DepartmentTimetableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentTimetableService {

    @Autowired
    private DepartmentTimetableRepository timetableRepository;

    public List<DepartmentTimetable> getAllTimetables() {
        return timetableRepository.findAll();
    }

    public List<DepartmentTimetable> getTimetablesByDepartment(Integer departmentId) {
        return timetableRepository.findByDepartmentDepartmentId(departmentId);
    }

    public DepartmentTimetable saveTimetable(DepartmentTimetable timetable) {
        return timetableRepository.save(timetable);
    }
}