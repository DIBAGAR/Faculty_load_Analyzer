package com.example.facultyload.controller;

import com.example.facultyload.dto.AssignedWorkDTO;
import com.example.facultyload.dto.FacultyDTO;
import com.example.facultyload.dto.FacultyDashboardDTO;
import com.example.facultyload.service.FacultyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    // Get individual faculty dashboard
    @GetMapping("/{id}/dashboard")
    public FacultyDashboardDTO getDashboard(@PathVariable Integer id) {
        return facultyService.getFacultyDashboard(id);
    }

    // Get assigned work for a faculty
    @GetMapping("/{id}/assigned-work")
    public List<AssignedWorkDTO> getAssignedWork(@PathVariable Integer id) {
        return facultyService.getAssignedWork(id);
    }

    // Get faculty profile
    @GetMapping("/{id}")
    public FacultyDTO getFacultyProfile(@PathVariable Integer id) {
        return facultyService.getFacultyById(id);
    }
}