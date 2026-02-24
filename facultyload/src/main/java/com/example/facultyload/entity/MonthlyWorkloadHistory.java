package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "monthly_workload_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_monthly_workload_history_faculty_month",
                        columnNames = {"faculty_id", "month_start_date"}
                )
        },
        indexes = {
                @Index(name = "idx_monthly_workload_history_faculty_id", columnList = "faculty_id"),
                @Index(name = "idx_monthly_workload_history_month", columnList = "month_start_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyWorkloadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    /**
     * First day of the month in UTC (e.g., 2026-02-01).
     */
    @Column(name = "month_start_date", nullable = false)
    private LocalDate monthStartDate;

    @Column(name = "monthly_assigned_hours", nullable = false)
    private Integer monthlyAssignedHours;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}

