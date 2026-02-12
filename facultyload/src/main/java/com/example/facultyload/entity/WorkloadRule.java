package com.example.facultyload.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "workload_rule")
public class WorkloadRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ruleId;

    private Integer maxHoursPerWeek;
    private Integer minHoursPerWeek;

    // Getters and Setters
    // ...
}