package com.example.facultyload.service;

import com.example.facultyload.entity.LeaveRequest;

public interface WorkloadRedistributionService {
    void redistributeForEmergencyLeave(LeaveRequest leaveRequest);
}

