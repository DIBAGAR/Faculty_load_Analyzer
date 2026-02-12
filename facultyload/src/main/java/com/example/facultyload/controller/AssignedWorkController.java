package com.example.facultyload.controller;

import com.example.facultyload.entity.AssignedWork;
import com.example.facultyload.repository.AssignedWorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/assigned-work")
public class AssignedWorkController {

    @Autowired
    private AssignedWorkRepository assignedWorkRepository;

    // Create
    @PostMapping
    public AssignedWork create(@RequestBody AssignedWork work) {
        return assignedWorkRepository.save(work);
    }

    // Read all
    @GetMapping
    public List<AssignedWork> getAll() {
        return assignedWorkRepository.findAll();
    }

    // Read by weekStartDate
    @GetMapping("/week")
    public List<AssignedWork> getByWeek(@RequestParam String start, @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return assignedWorkRepository.findByWeekStartDateBetween(startDate, endDate);
    }

    // Update
    @PutMapping("/{id}")
    public AssignedWork update(@PathVariable Integer id, @RequestBody AssignedWork work) {
        work.setId(id);
        return assignedWorkRepository.save(work);
    }

    // Delete
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        assignedWorkRepository.deleteById(id);
        return "Deleted ID: " + id;
    }
}