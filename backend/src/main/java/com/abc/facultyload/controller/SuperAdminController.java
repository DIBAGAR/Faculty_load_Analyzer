package com.abc.facultyload.controller;

import com.abc.facultyload.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getSuperAdminStats());
    }

    @GetMapping("/admins")
    public ResponseEntity<List<Map<String, Object>>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @PostMapping("/admins")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(adminService.createAdmin(payload));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable("id") Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok().build();
    }
}
