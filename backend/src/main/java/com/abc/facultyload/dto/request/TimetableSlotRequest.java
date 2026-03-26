package com.abc.facultyload.dto.request;

import lombok.Data;

@Data
public class TimetableSlotRequest {
    private Integer dayOfWeek;  // 1-6 (Mon-Sat)
    private Integer hour;       // 1-7
    private Long courseId;
    private Long venueId;
    private Long defaultFacultyId;
    private Long additionalFacultyId;
    private String slotType;    // THEORY or LAB
}
