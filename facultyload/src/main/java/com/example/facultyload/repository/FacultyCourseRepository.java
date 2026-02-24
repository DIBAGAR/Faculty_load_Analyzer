package com.example.facultyload.repository;

import com.example.facultyload.entity.CourseKnowledgeType;
import com.example.facultyload.entity.Faculty;
import com.example.facultyload.entity.FacultyCourse;
import com.example.facultyload.entity.FacultyCourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacultyCourseRepository extends JpaRepository<FacultyCourse, FacultyCourseId> {

    @Query("""
            select f from FacultyCourse fc
            join fc.faculty f
            where f.department.id = :departmentId
              and f.isActive = true
              and fc.course.id = :courseId
              and fc.knowledgeType = :knowledgeType
            order by f.monthlyAssignedHours asc, f.totalAssignedHours asc, f.id asc
            """)
    List<Faculty> findCandidates(
            @Param("departmentId") Long departmentId,
            @Param("courseId") Long courseId,
            @Param("knowledgeType") CourseKnowledgeType knowledgeType
    );
}

