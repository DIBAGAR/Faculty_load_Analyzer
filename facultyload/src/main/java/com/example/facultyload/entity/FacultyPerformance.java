package com.example.facultyload.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty_performance")
public class FacultyPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer performanceId;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    private Integer month;
    private Integer year;
    private Integer hoursWorked;
    private Integer tasksCompleted;
    private Integer rating;

    // Getters and Setters
    // ...
}