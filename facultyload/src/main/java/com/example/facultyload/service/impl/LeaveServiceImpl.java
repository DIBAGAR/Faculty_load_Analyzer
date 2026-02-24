package com.example.facultyload.service.impl;

import com.example.facultyload.dto.leave.LeaveCreateRequest;
import com.example.facultyload.dto.leave.LeaveDecisionRequest;
import com.example.facultyload.dto.leave.LeaveResponse;
import com.example.facultyload.entity.Faculty;
import com.example.facultyload.entity.LeaveRequest;
import com.example.facultyload.entity.LeaveStatus;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.exception.ForbiddenException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.FacultyRepository;
import com.example.facultyload.repository.LeaveRequestRepository;
import com.example.facultyload.service.LeaveService;
import com.example.facultyload.service.WorkloadRedistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final FacultyRepository facultyRepository;
    private final WorkloadRedistributionService workloadRedistributionService;

    @Override
    @Transactional
    public LeaveResponse submit(LeaveCreateRequest request, String facultyEmail) {
        if (request.fromDate().isAfter(request.toDate())) {
            throw new BadRequestException("fromDate must be <= toDate");
        }
        Faculty faculty = facultyRepository.findByUserEmailIgnoreCase(facultyEmail)
                .orElseThrow(() -> new NotFoundException("Faculty not found for user: " + facultyEmail));

        LeaveRequest lr = leaveRequestRepository.save(LeaveRequest.builder()
                .faculty(faculty)
                .fromDate(request.fromDate())
                .toDate(request.toDate())
                .leaveType(request.leaveType())
                .hourNumber(request.hourNumber())
                .isEmergency(request.isEmergency())
                .status(request.isEmergency() ? LeaveStatus.APPROVED : LeaveStatus.PENDING)
                .approvedBy(null)
                .build());

        if (request.isEmergency()) {
            workloadRedistributionService.redistributeForEmergencyLeave(lr);
        }

        return toResponse(lr);
    }

    @Override
    @Transactional
    public LeaveResponse decide(Long leaveId, LeaveDecisionRequest request, String approverEmail) {
        LeaveRequest lr = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new NotFoundException("Leave not found: " + leaveId));

        Faculty approver = facultyRepository.findByUserEmailIgnoreCase(approverEmail)
                .orElseThrow(() -> new NotFoundException("Approver faculty not found: " + approverEmail));

        if (!approver.isHod() || !approver.getDepartment().getId().equals(lr.getFaculty().getDepartment().getId())) {
            throw new ForbiddenException("Only department HOD can approve/reject leave");
        }

        if (request.status() == LeaveStatus.PENDING) {
            throw new BadRequestException("Decision must be APPROVED or REJECTED");
        }

        lr.setStatus(request.status());
        lr.setApprovedBy(approver);
        return toResponse(leaveRequestRepository.save(lr));
    }

    @Override
    public List<LeaveResponse> myLeaves(String facultyEmail) {
        Faculty faculty = facultyRepository.findByUserEmailIgnoreCase(facultyEmail)
                .orElseThrow(() -> new NotFoundException("Faculty not found for user: " + facultyEmail));
        return leaveRequestRepository.findByFacultyIdOrderByCreatedAtDesc(faculty.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private LeaveResponse toResponse(LeaveRequest lr) {
        return new LeaveResponse(
                lr.getId(),
                lr.getFaculty().getId(),
                lr.getFromDate(),
                lr.getToDate(),
                lr.getLeaveType(),
                lr.getHourNumber(),
                lr.getStatus(),
                Boolean.TRUE.equals(lr.getIsEmergency()),
                lr.getApprovedBy() != null ? lr.getApprovedBy().getId() : null
        );
    }
}

