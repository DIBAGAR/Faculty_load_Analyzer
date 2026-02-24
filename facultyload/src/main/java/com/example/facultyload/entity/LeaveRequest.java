package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(
        name = "leave_requests",
        indexes = {
                @Index(name = "idx_leave_requests_faculty_id", columnList = "faculty_id"),
                @Index(name = "idx_leave_requests_status", columnList = "status"),
                @Index(name = "idx_leave_requests_from_to", columnList = "from_date,to_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    @Builder.Default
    private LeaveType leaveType = LeaveType.OTHER;

    /**
     * If set, leave is only for a specific hour number (1..7) on each day in range.
     * If null, leave is for the full day(s).
     */
    @Column(name = "hour_number")
    private Integer hourNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "is_emergency")
    @Builder.Default
    private Boolean isEmergency = false;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Faculty approvedBy;

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