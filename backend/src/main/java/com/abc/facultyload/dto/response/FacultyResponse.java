package com.abc.facultyload.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResponse {
    private Long id;
    private String name;
    private String rollNumber;
    private String email;
    private String phone;
    private String bloodGroup;
    private String departmentName;
    private Long departmentId;
    private String role;
    private boolean isActive;
    private Integer currentMonthHours;
}
