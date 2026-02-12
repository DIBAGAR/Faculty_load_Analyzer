package com.example.facultyload.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subject")
public class Subject {

    @Id
    @Column(length = 20)
    private String subjectCode;

    private String subjectName;
    private Integer credits;

    // ---- GETTERS & SETTERS ----
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
}