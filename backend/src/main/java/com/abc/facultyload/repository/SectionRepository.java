package com.abc.facultyload.repository;

import com.abc.facultyload.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findAllByDepartmentId(Long deptId);
    Optional<Section> findByDepartmentIdAndYearAndSemesterAndSectionName(
        Long deptId, Integer year, Integer semester, String sectionName);
    boolean existsByDepartmentIdAndYearAndSemesterAndSectionName(
        Long deptId, Integer year, Integer semester, String sectionName);
}
