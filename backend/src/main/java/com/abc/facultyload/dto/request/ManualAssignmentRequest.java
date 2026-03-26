package com.abc.facultyload.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ManualAssignmentRequest {
    private Long deptId;
    private LocalDate date;
    private Integer hour;
    private Long courseId;
    private Long venueId;
    private Long facultyId;
    private String slotType; // THEORY or LAB
}
