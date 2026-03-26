package com.abc.facultyload.repository;

import com.abc.facultyload.entity.LabCourseMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabCourseMappingRepository extends JpaRepository<LabCourseMapping, Long> {

    List<LabCourseMapping> findAllByVenueId(Long venueId);

    @Query("SELECT m FROM LabCourseMapping m WHERE m.venue.department.id = :deptId")
    List<LabCourseMapping> findAllByDeptId(Long deptId);

    boolean existsByVenueIdAndCourseId(Long venueId, Long courseId);
}
