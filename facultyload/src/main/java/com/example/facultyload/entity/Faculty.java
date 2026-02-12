package com.example.facultyload.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer facultyId;

    private String name;
    private String email;
    private String role;  // admin/hod/faculty
    private Integer weeklyHours; // optional: track weekly workload

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    // ---- GETTERS & SETTERS ----
    public Integer getFacultyId() { return facultyId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}