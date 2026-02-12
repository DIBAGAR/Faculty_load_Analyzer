package com.example.facultyload.controller;

import com.example.facultyload.dto.FacultyDashboardDTO;
import com.example.facultyload.dto.TimetableDTO;
import com.example.facultyload.service.HODService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hod")
public class HodController {

    @Autowired
    private HODService hodService;

    // Get timetable of department
    @GetMapping("/timetable/{department}")
    public List<TimetableDTO> getTimetable(@PathVariable String department) {
        return hodService.getTimetableByDepartment(department);
    }

    // Get dashboard of all faculty in department
    @GetMapping("/dashboard/{department}")
    public List<FacultyDashboardDTO> getFacultyDashboard(@PathVariable String department) {
        return hodService.getFacultyDashboardByDepartment(department);
    }
}