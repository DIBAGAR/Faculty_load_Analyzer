package com.example.facultyload.dto;
import lombok.Data;

@Data
public class TimetableDTO {
    private String day;
    private String period;
    private String subjectName;
    private String facultyName;
    private String className;

    // constructors, getters, setters
}