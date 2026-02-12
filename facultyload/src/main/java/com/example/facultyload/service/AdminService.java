package com.example.facultyload.service;

import com.example.facultyload.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    public List<UserDTO> getAllUsers() {
        return new ArrayList<>(); // placeholder
    }

    public UserDTO saveUser(UserDTO dto) {
        return dto; // placeholder
    }

    public UserDTO updateUser(UserDTO dto) {
        return dto; // placeholder
    }

    public void deleteUser(Integer id) {
        // placeholder
    }
}