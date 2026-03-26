package com.abc.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private RoleName name;

    public enum RoleName {
        SUPER_ADMIN, FACULTY_ADMIN, DEPARTMENT_ADMIN, COURSE_ADMIN, VENUE_ADMIN,
        HOD, FACULTY, TEMP_HOD
    }
}
