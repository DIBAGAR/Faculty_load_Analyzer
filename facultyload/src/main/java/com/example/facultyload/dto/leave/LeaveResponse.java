package com.example.facultyload.dto.leave;

import com.example.facultyload.entity.LeaveStatus;
import com.example.facultyload.entity.LeaveType;

import java.time.LocalDate;

public record LeaveResponse(
        Long id,
        Long facultyId,
        LocalDate fromDate,
        LocalDate toDate,
        LeaveType leaveType,
        Integer hourNumber,
        LeaveStatus status,
        boolean isEmergency,
        Long approvedByFacultyId
) {
}

