package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "timetables",
        indexes = {
                @Index(name = "idx_timetables_department_id", columnList = "department_id"),
                @Index(name = "idx_timetables_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "year_of_study", nullable = false)
    private Integer yearOfStudy;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false, length = 10)
    private String section;

    @Column(name = "version_no")
    private Integer versionNo;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Faculty createdBy;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
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