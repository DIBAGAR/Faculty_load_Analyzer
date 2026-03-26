package com.abc.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Entity
@Table(name = "work_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"faculty_id", "assign_date", "hour"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timetable_slot_id", nullable = true) // Nullable because timetables can now be edited/deleted without destroying historical records
    private TimetableSlot timetableSlot;

    // Direct metadata snapshot so historical work assignments aren't corrupted if the timetable is changed
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = true)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", length = 10, nullable = true)
    private TimetableSlot.SlotType slotType;

    @Column(name = "assign_date", nullable = false)
    private LocalDate assignDate;

    @Column(name = "hour", nullable = false)
    private Integer hour;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Builder.Default
    @Column(name = "is_reassigned")
    private boolean reassigned = false;
}
