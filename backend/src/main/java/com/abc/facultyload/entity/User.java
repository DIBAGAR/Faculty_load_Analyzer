package com.abc.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "roll_number", unique = true, length = 50)
    private String rollNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "name", length = 150)
    private String name;
}
