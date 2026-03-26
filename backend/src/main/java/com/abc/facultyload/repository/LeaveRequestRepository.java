package com.abc.facultyload.repository;

import com.abc.facultyload.entity.LeaveRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findAllByFacultyDepartmentIdOrderByCreatedAtDesc(Long deptId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.department.id = :deptId ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByDeptIdOrdered(Long deptId);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM LeaveRequest lr WHERE lr.faculty.id = :facultyId")
    void deleteAllByFacultyId(Long facultyId);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE LeaveRequest lr SET lr.tempHod = null WHERE lr.tempHod.id = :facultyId")
    void nullifyTempHod(Long facultyId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.id = :facultyId ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByFacultyIdOrdered(Long facultyId);

    @Query("SELECT CASE WHEN COUNT(lr) > 0 THEN true ELSE false END " +
           "FROM LeaveRequest lr WHERE lr.faculty.id = :facultyId " +
           "AND lr.status IN ('APPROVED', 'PENDING') " +
           "AND lr.fromDate <= :toDate AND lr.toDate >= :fromDate")
    boolean hasOverlappingLeave(Long facultyId, LocalDate fromDate, LocalDate toDate);

    @Query("SELECT CASE WHEN COUNT(lr) > 0 THEN true ELSE false END " +
           "FROM LeaveRequest lr WHERE lr.faculty.id = :facultyId " +
           "AND lr.status = 'APPROVED' " +
           "AND lr.fromDate <= :toDate AND lr.toDate >= :fromDate")
    boolean hasApprovedLeaveInRange(Long facultyId, LocalDate fromDate, LocalDate toDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.user.role.name = 'HOD' " +
           "AND lr.faculty.department.id = :deptId ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findHodLeavesByDeptId(Long deptId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.department.id = :deptId " +
           "AND lr.faculty.user.role.name != 'HOD' AND lr.status = :status ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findPendingLeavesForHod(Long deptId, LeaveRequest.LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.user.role.name IN ('HOD', 'TEMP_HOD') " +
           "AND lr.status = :status ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findPendingLeavesForAdmin(LeaveRequest.LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.user.role.name IN ('HOD', 'TEMP_HOD') " +
           "AND lr.status IN ('APPROVED', 'REJECTED') ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findHistoryLeavesForAdmin(Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.department.id = :deptId " +
           "AND lr.faculty.user.role.name != 'HOD' AND lr.status IN ('APPROVED', 'REJECTED') ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findHistoryLeavesForHod(Long deptId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.faculty.department.id = :deptId " +
           "AND lr.status = 'APPROVED' AND lr.toDate >= :startDate")
    List<LeaveRequest> findApprovedLeavesForDeptSince(
        @org.springframework.data.repository.query.Param("deptId") Long deptId, 
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate);
}
