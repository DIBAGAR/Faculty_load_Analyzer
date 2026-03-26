package com.abc.facultyload.service;

import com.abc.facultyload.entity.Department;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<Department> getAllActiveDepartments() {
        return departmentRepository.findAllByActiveTrue();
    }

    @Transactional
    public Department createDepartment(String deptCode, String deptName) {
        if (departmentRepository.existsByDeptCode(deptCode)) {
            throw new AppException("Department code already exists", HttpStatus.BAD_REQUEST);
        }
        Department dept = Department.builder()
                .deptCode(deptCode)
                .deptName(deptName)
                .active(true)
                .build();
        return departmentRepository.save(dept);
    }

    @Transactional
    public Department updateDepartment(Long id, String deptCode, String deptName) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));

        if (!dept.getDeptCode().equals(deptCode) && departmentRepository.existsByDeptCode(deptCode)) {
            throw new AppException("Department code already exists", HttpStatus.BAD_REQUEST);
        }

        dept.setDeptCode(deptCode);
        dept.setDeptName(deptName);
        return departmentRepository.save(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));
        departmentRepository.delete(dept);
    }
}
