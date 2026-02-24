package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "faculty",
        indexes = {
                @Index(name = "idx_faculty_department_id", columnList = "department_id"),
                @Index(name = "idx_faculty_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private Designation designation;

    @Column(name = "is_hod", nullable = false)
    @Builder.Default
    private boolean isHod = false;

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<FacultyCourse> coursesKnown = new HashSet<>();

    @Column(name = "monthly_assigned_hours")
    @Builder.Default
    private Integer monthlyAssignedHours = 0;

    @Column(name = "total_assigned_hours")
    @Builder.Default
    private Integer totalAssignedHours = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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