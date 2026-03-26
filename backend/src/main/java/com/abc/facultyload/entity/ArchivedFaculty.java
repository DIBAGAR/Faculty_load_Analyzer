package com.abc.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "archived_faculty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ArchivedFaculty extends BaseEntity {

    @Column(name = "original_id", nullable = false)
    private Long originalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "roll_number", unique = true)
    private String rollNumber;

    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
}
