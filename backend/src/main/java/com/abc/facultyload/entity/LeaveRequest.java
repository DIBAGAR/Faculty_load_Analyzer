package com.abc.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "from_time")
    private LocalTime fromTime;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "to_time")
    private LocalTime toTime;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", length = 15)
    @Builder.Default
    private LeaveType type = LeaveType.LEAVE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "temp_hod_id")
    private Faculty tempHod;

    @Column(name = "rejection_reason", length = 300)
    private String rejectionReason;

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum LeaveType {
        LEAVE, ON_DUTY
    }
}
