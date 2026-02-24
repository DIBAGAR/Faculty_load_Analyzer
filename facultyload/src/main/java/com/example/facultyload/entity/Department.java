package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(
        name = "departments",
        indexes = {
                @Index(name = "idx_departments_code", columnList = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String code;

    @OneToOne
    @JoinColumn(name = "hod_id")
    private Faculty hod;

    @OneToOne
    @JoinColumn(name = "temp_hod_id")
    private Faculty tempHod;

    @Column(name = "temp_hod_end_date")
    private LocalDate tempHodEndDate;

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