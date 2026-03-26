package com.abc.facultyload.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkGenerationRequest {
    private Long deptId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer startHour;
    private Integer endHour;
}
