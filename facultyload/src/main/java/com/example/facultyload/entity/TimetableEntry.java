package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

@Entity
@Table(
        name = "timetable_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_timetable_entries_timetable_slot",
                        columnNames = {"timetable_id", "day_of_week", "hour_number"}
                )
        },
        indexes = {
                @Index(name = "idx_timetable_entries_timetable_id", columnList = "timetable_id"),
                @Index(name = "idx_timetable_entries_course_id", columnList = "course_id"),
                @Index(name = "idx_timetable_entries_venue_id", columnList = "venue_id"),
                @Index(name = "idx_timetable_entries_default_faculty_id", columnList = "default_faculty_id"),
                @Index(name = "idx_timetable_entries_active_slot", columnList = "department_id,year_of_study,section,semester,day_of_week,hour_number,is_active"),
                @Index(name = "idx_timetable_entries_active_venue", columnList = "venue_id,day_of_week,hour_number,is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;

    /**
     * Denormalized fields for DB-level conflict prevention. These MUST be kept in sync
     * with the parent timetable when activating/deactivating timetables.
     */
    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "year_of_study", nullable = false)
    private Integer yearOfStudy;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "section", nullable = false, length = 10)
    private String section;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "hour_number")
    private Integer hourNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionType type;

    @ManyToOne
    @JoinColumn(name = "default_faculty_id")
    private Faculty defaultFaculty;
}