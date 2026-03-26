package com.abc.facultyload.repository;

import com.abc.facultyload.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    List<Course> findAllByDepartmentIdAndSemester(Long deptId, Integer semester);
    List<Course> findAllByDepartmentId(Long deptId);
    List<Course> findAllByDepartmentIdAndActiveTrue(Long deptId);
    List<Course> findAllByActiveTrue();
    boolean existsByCourseCodeAndDepartmentId(String courseCode, Long deptId);
    Optional<Course> findByCourseCodeAndDepartmentId(String courseCode, Long deptId);
    long countByDepartmentId(Long deptId);
}
