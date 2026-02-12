package com.example.facultyload.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "class")
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer classId;

    @Column(nullable = false)
    private String className;

    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @OneToMany(mappedBy = "classEntity")
    private List<AssignedWork> assignedWorkList;

    @OneToMany(mappedBy = "classEntity")
    private List<DepartmentTimetable> timetableList;

    // Getters and Setters
    // ...
}