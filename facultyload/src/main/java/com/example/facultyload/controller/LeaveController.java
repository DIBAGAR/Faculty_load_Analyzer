package com.example.facultyload.controller;

import com.example.facultyload.dto.leave.LeaveCreateRequest;
import com.example.facultyload.dto.leave.LeaveDecisionRequest;
import com.example.facultyload.dto.leave.LeaveResponse;
import com.example.facultyload.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PreAuthorize("hasRole('FACULTY') or hasRole('HOD')")
    @PostMapping
    public LeaveResponse submit(@Valid @RequestBody LeaveCreateRequest request, Authentication authentication) {
        return leaveService.submit(request, authentication.getName());
    }

    @PreAuthorize("hasRole('FACULTY') or hasRole('HOD')")
    @GetMapping("/me")
    public List<LeaveResponse> myLeaves(Authentication authentication) {
        return leaveService.myLeaves(authentication.getName());
    }

    @PreAuthorize("hasRole('HOD') or hasRole('SUPER_ADMIN')")
    @PostMapping("/{leaveId}/decision")
    public LeaveResponse decide(@PathVariable Long leaveId, @Valid @RequestBody LeaveDecisionRequest request, Authentication authentication) {
        return leaveService.decide(leaveId, request, authentication.getName());
    }
}

