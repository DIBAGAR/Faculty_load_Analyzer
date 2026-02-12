package com.example.facultyload.dto;

import lombok.Data;
import java.util.List;

@Data
public class FacultyDashboardDTO {
    private FacultyDTO faculty;
    private List<AssignedWorkDTO> assignedWork;
    private Integer weeklyHours;
}