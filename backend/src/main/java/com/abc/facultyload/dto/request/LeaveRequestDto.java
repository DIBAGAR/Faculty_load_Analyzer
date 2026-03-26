package com.abc.facultyload.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LeaveRequestDto {
    private LocalDate fromDate;
    private LocalTime fromTime;
    private LocalDate toDate;
    private LocalTime toTime;
    private String reason;
    private com.abc.facultyload.entity.LeaveRequest.LeaveType type;
    private Long tempHodId; // relevant only for HOD leave
}
