package com.example.facultyload.dto.leave;

import com.example.facultyload.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public record LeaveDecisionRequest(
        @NotNull LeaveStatus status
) {
}

