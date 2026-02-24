package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_courses_course_code", columnList = "course_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer credit;

    @ManyToMany
    @JoinTable(
            name = "course_departments",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_course_departments_course_dept", columnNames = {"course_id", "department_id"})
    )
    @Builder.Default
    private Set<Department> departments = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}