package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(
        name = "assigned_work",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_assigned_work_entry_date",
                        columnNames = {"timetable_entry_id", "work_date"}
                )
        },
        indexes = {
                @Index(name = "idx_assigned_work_faculty_id", columnList = "faculty_id"),
                @Index(name = "idx_assigned_work_work_date", columnList = "work_date"),
                @Index(name = "idx_assigned_work_week_start", columnList = "week_start_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(optional = false)
    @JoinColumn(name = "timetable_entry_id")
    private TimetableEntry timetableEntry;

    @Column(name = "work_date")
    private LocalDate workDate;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(nullable = false, length = 30)
    private String status;

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