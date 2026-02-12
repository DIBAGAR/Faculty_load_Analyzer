package com.example.facultyload.service;

import com.example.facultyload.entity.Subject;
import com.example.facultyload.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectByCode(String code) {
        return subjectRepository.findById(code).orElse(null);
    }

    public Subject saveSubject(Subject subject) {
        return subjectRepository.save(subject);
    }
}