package com.abc.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "timetable_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TimetableSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Mon ... 6=Sat

    @Column(name = "hour", nullable = false)
    private Integer hour; // 1-7

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_faculty_id")
    private Faculty defaultFaculty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "additional_faculty_id")
    private Faculty additionalFaculty;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", length = 10)
    @Builder.Default
    private SlotType slotType = SlotType.THEORY;

    public enum SlotType {
        THEORY, LAB
    }
}
