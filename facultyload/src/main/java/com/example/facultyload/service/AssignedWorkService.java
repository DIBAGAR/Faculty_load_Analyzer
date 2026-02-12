package com.example.facultyload.service;

import com.example.facultyload.entity.AssignedWork;
import com.example.facultyload.repository.AssignedWorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignedWorkService {

    @Autowired
    private AssignedWorkRepository assignedWorkRepository;

    public List<AssignedWork> getAllAssignedWork() {
        return assignedWorkRepository.findAll();
    }

    public List<AssignedWork> getAssignedWorkByFaculty(Integer facultyId) {
        return assignedWorkRepository.findByFacultyFacultyId(facultyId);
    }

    public List<AssignedWork> getAssignedWorkByWeek(LocalDate start, LocalDate end) {
        return assignedWorkRepository.findByWeekStartDateBetween(start, end);
    }

    public AssignedWork saveAssignedWork(AssignedWork work) {
        return assignedWorkRepository.save(work);
    }
}