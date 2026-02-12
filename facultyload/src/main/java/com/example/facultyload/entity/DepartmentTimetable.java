package com.example.facultyload.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "department_timetable")
public class DepartmentTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String day;
    private String period;
    private String subject;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;  // your class entity
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "year_id") // matches the foreign key column
    private Year year;

    // Getter and Setter
    public Year getYear() { return year; }
    public void setYear(Year year) { this.year = year; }
    // ---- GETTERS & SETTERS ----
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    public ClassEntity getClassEntity() { return classEntity; }
    public void setClassEntity(ClassEntity classEntity) { this.classEntity = classEntity; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}