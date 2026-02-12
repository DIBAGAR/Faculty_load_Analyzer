package com.example.facultyload.dto;

import lombok.Data;

@Data
public class FacultyDTO {
    private Integer id;               // matches setId()
    private String name;              
    private String email;
    private String role;
    private String departmentName;    // matches setDepartmentName()
}