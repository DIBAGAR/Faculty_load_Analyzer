package com.abc.facultyload.repository;

import com.abc.facultyload.entity.CourseFacultyMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CourseFacultyMappingRepository extends JpaRepository<CourseFacultyMapping, Long>,
        JpaSpecificationExecutor<CourseFacultyMapping> {

    List<CourseFacultyMapping> findAllByFacultyId(Long facultyId);
    List<CourseFacultyMapping> findAllByCourseId(Long courseId);

    @Query("SELECT cfm FROM CourseFacultyMapping cfm WHERE cfm.course.id = :courseId AND cfm.type = :type")
    List<CourseFacultyMapping> findByCourseIdAndType(Long courseId, CourseFacultyMapping.MappingType type);

    boolean existsByFacultyIdAndCourseId(Long facultyId, Long courseId);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM CourseFacultyMapping c WHERE c.faculty.id = :facultyId")
    void deleteAllByFacultyId(Long facultyId);
}
