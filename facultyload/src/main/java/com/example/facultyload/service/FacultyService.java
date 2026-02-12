package com.example.facultyload.service;

import com.example.facultyload.dto.AssignedWorkDTO;
import com.example.facultyload.dto.FacultyDTO;
import com.example.facultyload.dto.FacultyDashboardDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacultyService {

    public FacultyDashboardDTO getFacultyDashboard(Integer id) {
        return new FacultyDashboardDTO(); // placeholder
    }

    public List<AssignedWorkDTO> getAssignedWork(Integer id) {
        return new ArrayList<>(); // placeholder
    }

    public FacultyDTO getFacultyById(Integer id) {
        return new FacultyDTO(); // placeholder
    }
}