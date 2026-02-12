package com.example.facultyload.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty_availability")
public class FacultyAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer availabilityId;

    @ManyToOne
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    private String dayOfWeek;
    private String timeSlot;
    private Integer availableHours;

    // Getters and Setters
    // ...
}