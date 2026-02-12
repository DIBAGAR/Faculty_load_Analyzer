package com.example.facultyload.service;

import com.example.facultyload.entity.Users;
import com.example.facultyload.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    public Users saveUser(Users user) {
        return usersRepository.save(user);
    }

    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public void deleteUser(Integer userId) {
        usersRepository.deleteById(userId);
    }
}