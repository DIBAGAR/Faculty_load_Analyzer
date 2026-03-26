package com.abc.facultyload.repository;

import com.abc.facultyload.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByActiveTrue();
    boolean existsByDeptCode(String deptCode);
    long count();
}
