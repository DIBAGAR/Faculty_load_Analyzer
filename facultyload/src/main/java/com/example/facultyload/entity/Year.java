package com.example.facultyload.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "year")
public class Year {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer yearId;

    @Column(nullable = false)
    private String yearName;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "year")
    private List<Semester> semesters;

    @OneToMany(mappedBy = "year")
    private List<AssignedWork> assignedWorkList;

    @OneToMany(mappedBy = "year")
    private List<DepartmentTimetable> timetableList;

    // Getters and Setters
    // ...
}