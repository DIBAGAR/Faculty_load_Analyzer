package com.example.facultyload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.facultyload.entity.WorkloadRule;

public interface WorkloadRuleRepository extends JpaRepository<WorkloadRule, Integer> { }