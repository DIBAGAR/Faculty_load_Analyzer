package com.abc.facultyload.controller;

import com.abc.facultyload.entity.Department;
import com.abc.facultyload.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllActiveDepartments());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(departmentService.createDepartment(payload.get("deptCode"), payload.get("deptName")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, payload.get("deptCode"), payload.get("deptName")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable("id") Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }
}
