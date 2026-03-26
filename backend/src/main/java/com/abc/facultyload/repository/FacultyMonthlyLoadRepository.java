package com.abc.facultyload.repository;

import com.abc.facultyload.entity.FacultyMonthlyLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FacultyMonthlyLoadRepository extends JpaRepository<FacultyMonthlyLoad, Long> {
    Optional<FacultyMonthlyLoad> findByFacultyIdAndYearAndMonth(Long facultyId, Integer year, Integer month);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM FacultyMonthlyLoad fml WHERE fml.faculty.id = :facultyId")
    void deleteAllByFacultyId(Long facultyId);

    @Query("SELECT fml FROM FacultyMonthlyLoad fml WHERE fml.faculty.department.id = :deptId AND fml.year = :year AND fml.month = :month")
    List<FacultyMonthlyLoad> findByDeptIdAndPeriod(Long deptId, Integer year, Integer month);

    @Query("SELECT COALESCE(SUM(fml.totalHours), 0) FROM FacultyMonthlyLoad fml WHERE fml.faculty.department.id = :deptId AND fml.year = :year AND fml.month = :month")
    Integer sumHoursByDeptIdAndPeriod(Long deptId, Integer year, Integer month);
}
