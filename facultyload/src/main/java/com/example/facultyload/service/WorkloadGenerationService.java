package com.example.facultyload.service;

import com.example.facultyload.dto.workload.WorkloadGenerationResponse;

public interface WorkloadGenerationService {
    WorkloadGenerationResponse generateNextWeekWork(Long departmentId, Integer yearOfStudy, String section, String requesterEmail);
}

