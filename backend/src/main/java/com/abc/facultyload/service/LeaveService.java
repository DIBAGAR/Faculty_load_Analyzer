package com.abc.facultyload.service;

import com.abc.facultyload.dto.request.LeaveRequestDto;
import com.abc.facultyload.entity.*;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;
    private final WorkAssignmentService workAssignmentService;

    @Transactional
    public LeaveRequest applyLeave(Long facultyId, LeaveRequestDto dto) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new AppException("Faculty not found", HttpStatus.NOT_FOUND));

        if (leaveRequestRepository.hasOverlappingLeave(facultyId, dto.getFromDate(), dto.getToDate())) {
            throw new AppException("You already have a leave request during this period.", HttpStatus.BAD_REQUEST);
        }

        Faculty tempHod = null;
        if (dto.getTempHodId() != null) {
            tempHod = facultyRepository.findById(dto.getTempHodId())
                    .orElseThrow(() -> new AppException("Temp HOD faculty not found", HttpStatus.NOT_FOUND));
        }

        LocalTime startT = dto.getFromTime() != null ? dto.getFromTime() : LocalTime.of(0, 0);
        LocalTime endT = dto.getToTime() != null ? dto.getToTime() : LocalTime.of(23, 59);
        LocalDateTime startDT = LocalDateTime.of(dto.getFromDate(), startT);
        LocalDateTime endDT = LocalDateTime.of(dto.getToDate(), endT);

        if (Duration.between(startDT, endDT).toHours() < 5) {
            throw new AppException("Leave duration must be at least 5 hours.", HttpStatus.BAD_REQUEST);
        }

        LeaveRequest leave = LeaveRequest.builder()
                .faculty(faculty)
                .fromDate(dto.getFromDate())
                .fromTime(dto.getFromTime())
                .toDate(dto.getToDate())
                .toTime(dto.getToTime())
                .reason(dto.getReason())
                .status(LeaveRequest.LeaveStatus.PENDING)
                .tempHod(tempHod)
                .type(dto.getType() != null ? dto.getType() : LeaveRequest.LeaveType.LEAVE)
                .build();

        return leaveRequestRepository.save(leave);
    }

    @Transactional
    public void cancelLeave(Long leaveId, Long facultyId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new AppException("Leave request not found", HttpStatus.NOT_FOUND));
                
        if (!leave.getFaculty().getId().equals(facultyId)) {
            throw new AppException("You can only cancel your own leave requests.", HttpStatus.FORBIDDEN);
        }
        
        if (leave.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new AppException("You can only cancel PENDING leave requests.", HttpStatus.BAD_REQUEST);
        }
        
        leaveRequestRepository.delete(leave);
    }

    @Transactional
    public Map<String, Object> approveLeave(Long leaveId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new AppException("Leave request not found", HttpStatus.NOT_FOUND));

        leave.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leaveRequestRepository.save(leave);

        Faculty faculty = leave.getFaculty();
        boolean isHod = faculty.getUser().getRole().getName() == Role.RoleName.HOD;

        // Assign TempHOD role if HOD leave
        if (isHod && leave.getTempHod() != null) {
            Role tempHodRole = roleRepository.findByName(Role.RoleName.TEMP_HOD).orElseThrow();
            leave.getTempHod().getUser().setRole(tempHodRole);
            userRepository.save(leave.getTempHod().getUser());
        }

        // Notify faculty
        notificationService.createAndSendNotification(faculty.getUser(), "Leave Approved",
                "Your leave from " + leave.getFromDate() + " to " + leave.getToDate() + " has been approved.", true);

        // Synchronous reassignment — returns unresolved slots for HOD popup
        List<Map<String, Object>> unresolved = workAssignmentService.reassignWorkForLeave(
                faculty, leave.getFromDate(), leave.getToDate());

        Map<String, Object> result = new HashMap<>();
        result.put("leaveId", leave.getId());
        result.put("facultyName", faculty.getName());
        result.put("fromDate", leave.getFromDate().toString());
        result.put("toDate", leave.getToDate().toString());
        result.put("unresolvedSlots", unresolved);
        return result;
    }

    @Transactional
    public LeaveRequest rejectLeave(Long leaveId, String reason) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new AppException("Leave request not found", HttpStatus.NOT_FOUND));
        leave.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leave.setRejectionReason(reason);
        leaveRequestRepository.save(leave);

        notificationService.createAndSendNotification(leave.getFaculty().getUser(), "Leave Rejected",
                "Your leave request has been rejected. Reason: " + reason, true);
        return leave;
    }

    public List<LeaveRequest> getDeptLeaveRequests(Long deptId) {
        return leaveRequestRepository.findByDeptIdOrdered(deptId);
    }

    public List<LeaveRequest> getFacultyLeaveHistory(Long facultyId) {
        return leaveRequestRepository.findByFacultyIdOrdered(facultyId);
    }

    public List<LeaveRequest> getPendingLeavesForHod(Long deptId) {
        return leaveRequestRepository.findPendingLeavesForHod(deptId, LeaveRequest.LeaveStatus.PENDING);
    }

    public List<LeaveRequest> getPendingLeavesForAdmin() {
        return leaveRequestRepository.findPendingLeavesForAdmin(LeaveRequest.LeaveStatus.PENDING);
    }

    public List<LeaveRequest> getHistoryLeavesForAdmin() {
        return leaveRequestRepository.findHistoryLeavesForAdmin(PageRequest.of(0, 100));
    }

    public List<LeaveRequest> getHistoryLeavesForHod(Long deptId) {
        return leaveRequestRepository.findHistoryLeavesForHod(deptId, PageRequest.of(0, 100));
    }

    // Scheduled: revoke TempHOD role when HOD leave ends
    @Scheduled(cron = "0 * * * * *") // runs every minute
    @Transactional
    public void revokeTempHodAccess() {
        LocalDateTime now = LocalDateTime.now();
        List<LeaveRequest> expiredLeaves = leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveRequest.LeaveStatus.APPROVED
                        && lr.getTempHod() != null
                        && lr.getTempHod().getUser().getRole().getName() == Role.RoleName.TEMP_HOD)
                .filter(lr -> {
                    LocalTime tTime = lr.getToTime() != null ? lr.getToTime() : LocalTime.of(23, 59);
                    LocalDateTime endDateTime = LocalDateTime.of(lr.getToDate(), tTime);
                    return now.isAfter(endDateTime);
                })
                .toList();

        if (expiredLeaves.isEmpty()) return;

        Role facultyRole = roleRepository.findByName(Role.RoleName.FACULTY).orElseThrow();
        for (LeaveRequest lr : expiredLeaves) {
            lr.getTempHod().getUser().setRole(facultyRole);
            userRepository.save(lr.getTempHod().getUser());
        }
    }
}
