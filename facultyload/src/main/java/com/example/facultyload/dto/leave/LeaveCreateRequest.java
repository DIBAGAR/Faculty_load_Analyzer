package com.example.facultyload.dto.leave;

import com.example.facultyload.entity.LeaveType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveCreateRequest(
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotNull LeaveType leaveType,
        @Min(1) @Max(7) Integer hourNumber,
        boolean isEmergency
) {
}

