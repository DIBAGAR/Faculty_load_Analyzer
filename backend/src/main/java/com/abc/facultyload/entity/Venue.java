package com.abc.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Venue extends BaseEntity {

    @Column(name = "block", nullable = false, length = 50)
    private String block;

    @Column(name = "venue_name", nullable = false, length = 100)
    private String venueName;

    @Enumerated(EnumType.STRING)
    @Column(name = "venue_type", nullable = false, length = 15)
    private VenueType venueType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dept_id")
    private Department department;

    @Column(name = "capacity")
    private Integer capacity;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    public enum VenueType {
        LAB, CLASSROOM
    }
}
