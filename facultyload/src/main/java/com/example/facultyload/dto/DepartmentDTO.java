package com.example.facultyload.dto;

import lombok.Data;

@Data
public class DepartmentDTO {
    private Integer id;
    private String departmentName;    // used in controller mapping
}