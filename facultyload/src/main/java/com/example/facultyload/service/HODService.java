package com.example.facultyload.service;

import com.example.facultyload.dto.FacultyDashboardDTO;
import com.example.facultyload.dto.TimetableDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HODService {

    public List<TimetableDTO> getTimetableByDepartment(String department) {
        return new ArrayList<>(); // placeholder
    }

    public List<FacultyDashboardDTO> getFacultyDashboardByDepartment(String department) {
        return new ArrayList<>(); // placeholder
    }
}