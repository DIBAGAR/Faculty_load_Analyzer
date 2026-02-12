package com.example.facultyload.controller;

import com.example.facultyload.dto.UserDTO;
import com.example.facultyload.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PostMapping("/user")
    public UserDTO createUser(@RequestBody UserDTO dto) {
        return adminService.saveUser(dto);
    }

    @PutMapping("/user/{id}")
    public UserDTO updateUser(@PathVariable Integer id, @RequestBody UserDTO dto) {
        dto.setId(id);
        return adminService.updateUser(dto);
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
    }
}