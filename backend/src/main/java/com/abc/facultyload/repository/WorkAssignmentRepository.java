package com.abc.facultyload.repository;

import com.abc.facultyload.entity.WorkAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkAssignmentRepository extends JpaRepository<WorkAssignment, Long> {

    List<WorkAssignment> findAllByFacultyIdAndAssignDateBetween(Long facultyId, LocalDate start, LocalDate end);

    @Query("SELECT CASE WHEN COUNT(wa) > 0 THEN true ELSE false END FROM WorkAssignment wa " +
           "WHERE wa.faculty.department.id = :deptId " +
           "AND wa.assignDate BETWEEN :startDate AND :endDate " +
           "AND wa.hour >= :startHour AND wa.hour <= :endHour " +
           "AND wa.timetableSlot IS NOT NULL")
    boolean existsInRange(Long deptId, LocalDate startDate, LocalDate endDate, Integer startHour, Integer endHour);

    Optional<WorkAssignment> findByFacultyIdAndAssignDateAndHour(Long facultyId, LocalDate date, Integer hour);

    @Transactional
    @Modifying
    @Query("DELETE FROM WorkAssignment w WHERE w.faculty.id = :facultyId")
    void deleteAllByFacultyId(Long facultyId);

    @Query("SELECT CASE WHEN COUNT(wa) > 0 THEN true ELSE false END FROM WorkAssignment wa " +
           "WHERE wa.venue.id = :venueId AND wa.assignDate = :date AND wa.hour = :hour")
    boolean venueConflictsAt(Long venueId, LocalDate date, Integer hour);

    List<WorkAssignment> findAllByFacultyIdAndAssignDateBetweenOrderByAssignDateAsc(Long facultyId, LocalDate from, LocalDate to);

    @Query("SELECT wa FROM WorkAssignment wa WHERE wa.faculty.department.id = :deptId " +
           "AND wa.assignDate BETWEEN :startDate AND :endDate " +
           "AND wa.hour >= :startHour AND wa.hour <= :endHour")
    List<WorkAssignment> findWorkToDelete(Long deptId, LocalDate startDate, LocalDate endDate, Integer startHour, Integer endHour);

    @Query("SELECT wa FROM WorkAssignment wa WHERE wa.faculty.id = :facultyId AND wa.assignDate BETWEEN :start AND :end")
    List<WorkAssignment> findReassignableWork(Long facultyId, LocalDate start, LocalDate end);

    @Transactional
    @Modifying
    @Query("UPDATE WorkAssignment wa SET wa.timetableSlot = null WHERE wa.timetableSlot IN :slots")
    void nullifyTimetableSlotIn(List<com.abc.facultyload.entity.TimetableSlot> slots);

    @Query("""
        SELECT DISTINCT wa.assignDate FROM WorkAssignment wa
        WHERE wa.faculty.department.id = :deptId
        AND wa.assignDate >= :startDate
        ORDER BY wa.assignDate ASC
        """)
    List<LocalDate> findDistinctWorkingDaysForDept(
        @org.springframework.data.repository.query.Param("deptId") Long deptId, 
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate);

    @Query("SELECT wa FROM WorkAssignment wa WHERE wa.faculty.department.id = :deptId " +
           "AND wa.assignDate BETWEEN :startDate AND :endDate " +
           "ORDER BY wa.assignDate ASC, wa.faculty.name ASC, wa.hour ASC")
    List<WorkAssignment> findByDeptAndDateRange(
        @org.springframework.data.repository.query.Param("deptId") Long deptId,
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);

    @Query("SELECT wa FROM WorkAssignment wa WHERE wa.faculty.department.id = :deptId " +
           "AND wa.faculty.id = :facultyId " +
           "AND wa.assignDate BETWEEN :startDate AND :endDate " +
           "ORDER BY wa.assignDate ASC, wa.hour ASC")
    List<WorkAssignment> findByDeptAndFacultyAndDateRange(
        @org.springframework.data.repository.query.Param("deptId") Long deptId,
        @org.springframework.data.repository.query.Param("facultyId") Long facultyId,
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);
}
