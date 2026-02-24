package com.example.facultyload.service;

import com.example.facultyload.dto.leave.LeaveCreateRequest;
import com.example.facultyload.dto.leave.LeaveDecisionRequest;
import com.example.facultyload.dto.leave.LeaveResponse;

import java.util.List;

public interface LeaveService {
    LeaveResponse submit(LeaveCreateRequest request, String facultyEmail);
    LeaveResponse decide(Long leaveId, LeaveDecisionRequest request, String approverEmail);
    List<LeaveResponse> myLeaves(String facultyEmail);
}

