package com.example.facultyload.controller;

import com.example.facultyload.dto.workload.WorkloadGenerationResponse;
import com.example.facultyload.service.WorkloadGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadGenerationService workloadGenerationService;

    @PreAuthorize("hasRole('HOD') or hasRole('SUPER_ADMIN')")
    @PostMapping("/generate-next-week")
    public WorkloadGenerationResponse generateNextWeek(
            @RequestParam Long departmentId,
            @RequestParam Integer yearOfStudy,
            @RequestParam String section,
            Authentication authentication
    ) {
        return workloadGenerationService.generateNextWeekWork(departmentId, yearOfStudy, section, authentication.getName());
    }
}

