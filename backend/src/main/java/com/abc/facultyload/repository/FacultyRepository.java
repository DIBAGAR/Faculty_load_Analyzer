package com.abc.facultyload.repository;

import com.abc.facultyload.entity.Faculty;
import com.abc.facultyload.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long>, JpaSpecificationExecutor<Faculty> {
    List<Faculty> findAllByActiveTrue();
    List<Faculty> findAllByDepartmentId(Long deptId);
    List<Faculty> findAllByDepartmentIdAndActiveTrue(Long deptId);
    Optional<Faculty> findByRollNumber(String rollNumber);
    Optional<Faculty> findByEmail(String email);
    Optional<Faculty> findByUserId(Long userId);

    @Query("SELECT f FROM Faculty f JOIN f.user u JOIN u.role r WHERE r.name = :roleName AND f.department.id = :deptId")
    Optional<Faculty> findHodByDeptId(Long deptId, Role.RoleName roleName);

    @Query("SELECT COUNT(f) FROM Faculty f WHERE f.department.id = :deptId AND f.active = true")
    Long countByDeptId(Long deptId);
}
