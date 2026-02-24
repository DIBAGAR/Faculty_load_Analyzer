package com.example.facultyload.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "faculty_course",
        indexes = {
                @Index(name = "idx_faculty_course_faculty_id", columnList = "faculty_id"),
                @Index(name = "idx_faculty_course_course_id", columnList = "course_id"),
                @Index(name = "idx_faculty_course_knowledge_type", columnList = "knowledge_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyCourse {

    @EmbeddedId
    private FacultyCourseId id;

    @ManyToOne
    @MapsId("facultyId")
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "knowledge_type", nullable = false, length = 20)
    private CourseKnowledgeType knowledgeType;
}