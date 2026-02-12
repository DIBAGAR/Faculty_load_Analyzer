package com.example.facultyload.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "semester")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer semesterId;

    @Column(nullable = false)
    private String semesterName;

    @ManyToOne
    @JoinColumn(name = "year_id", nullable = false)
    private Year year;

    @OneToMany(mappedBy = "semester")
    private List<ClassEntity> classes;

    @OneToMany(mappedBy = "semester")
    private List<AssignedWork> assignedWorkList;

    @OneToMany(mappedBy = "semester")
    private List<DepartmentTimetable> timetableList;
    
    // Getters and Setters
    // ...
}