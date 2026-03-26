package com.abc.facultyload.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ManualWorkAssignmentRequest {
    private LocalDate date;
    private Integer hour;
    private Long courseId;
    private Long venueId;
    private Long facultyId;
    private Long timetableSlotId;
    private String slotType; // THEORY or LAB
}
