package com.abc.facultyload.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FacultyRequest {
    @NotBlank private String name;
    @NotBlank private String rollNumber;
    @Email @NotBlank private String email;
    @NotBlank private String phone;
    @NotNull private Long departmentId;
    private String bloodGroup;
    private String password;
    @NotBlank private String role; // "HOD" or "FACULTY"
}
