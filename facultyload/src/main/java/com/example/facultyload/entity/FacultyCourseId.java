package com.example.facultyload.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FacultyCourseId implements Serializable {

    private Long facultyId;
    private Long courseId;
}