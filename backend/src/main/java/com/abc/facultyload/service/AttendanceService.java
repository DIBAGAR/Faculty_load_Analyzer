package com.abc.facultyload.service;

import com.abc.facultyload.entity.Department;
import com.abc.facultyload.entity.Faculty;
import com.abc.facultyload.entity.LeaveRequest;
import com.abc.facultyload.entity.LeaveRequest.LeaveType;
import com.abc.facultyload.repository.DepartmentRepository;
import com.abc.facultyload.repository.FacultyRepository;
import com.abc.facultyload.repository.LeaveRequestRepository;
import com.abc.facultyload.repository.WorkAssignmentRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final WorkAssignmentRepository workAssignmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @Data
    @Builder
    public static class FacultyAttendanceStat {
        private Long facultyId;
        private String facultyName;
        private String rollNumber;
        private int totalWorkingDays;
        private int presentDays;
        private int leaveDays;
        private int onDutyDays;
        private double percentage;
    }

    public List<FacultyAttendanceStat> getDeptAttendanceStats(Long deptId) {
        Department dept = departmentRepository.findById(deptId).orElseThrow();
        LocalDate startDate = dept.getAttendanceStartDate() != null ? dept.getAttendanceStartDate() : LocalDate.now().minusMonths(6);
        
        // 1. Get all distinct working days for this department
        List<LocalDate> workingDays = workAssignmentRepository.findDistinctWorkingDaysForDept(deptId, startDate);
        int totalWorkingDays = workingDays.size();

        // 2. Get all active faculties (except HOD maybe? Let's include everyone or just exclude super admin. We will fetch all dept faculty)
        List<Faculty> faculties = facultyRepository.findAllByDepartmentIdAndActiveTrue(deptId);

        // 3. Get all approved leaves since start date
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesForDeptSince(deptId, startDate);

        // Group leaves by faculty for quick access
        Map<Long, List<LeaveRequest>> leavesByFaculty = approvedLeaves.stream()
                .collect(Collectors.groupingBy(lr -> lr.getFaculty().getId()));

        List<FacultyAttendanceStat> stats = new ArrayList<>();

        for (Faculty f : faculties) {
            List<LeaveRequest> fLeaves = leavesByFaculty.getOrDefault(f.getId(), new ArrayList<>());
            
            int leaveDays = 0;
            int onDutyDays = 0;

            for (LocalDate wd : workingDays) {
                // Check if faculty has a leave covering this working day
                for (LeaveRequest lr : fLeaves) {
                    if (!wd.isBefore(lr.getFromDate()) && !wd.isAfter(lr.getToDate())) {
                        if (lr.getType() == LeaveType.ON_DUTY) {
                            onDutyDays++;
                        } else {
                            leaveDays++;
                        }
                        break; // Only count one leave per day
                    }
                }
            }

            // Calculations
            // Actual Present = Total (X) - Leave (L) - On Duty (O)
            int actualPresentDays = Math.max(0, totalWorkingDays - leaveDays - onDutyDays);
            
            // Percentage = (Actual Present + On Duty) / Total
            double percentage = 0.0;
            if (totalWorkingDays > 0) {
                percentage = ((double)(actualPresentDays + onDutyDays) / totalWorkingDays) * 100.0;
            }

            stats.add(FacultyAttendanceStat.builder()
                    .facultyId(f.getId())
                    .facultyName(f.getName())
                    .rollNumber(f.getRollNumber())
                    .totalWorkingDays(totalWorkingDays)
                    .presentDays(actualPresentDays)
                    .leaveDays(leaveDays)
                    .onDutyDays(onDutyDays)
                    .percentage(Math.round(percentage * 10.0) / 10.0)
                    .build());
        }

        return stats;
    }
    @Data
    @Builder
    public static class DailyAttendanceStat {
        private Long facultyId;
        private String facultyName;
        private String rollNumber;
        private String status; // PRESENT, ABSENT (on leave), ON_DUTY, NON_WORKING_DAY
    }

    public List<DailyAttendanceStat> getDailyAttendance(Long deptId, LocalDate date) {
        List<Faculty> faculties = facultyRepository.findAllByDepartmentIdAndActiveTrue(deptId);

        // Check if this is a working day (any work assigned for this dept on this date)
        List<LocalDate> workingDays = workAssignmentRepository.findDistinctWorkingDaysForDept(deptId, date.minusDays(1));
        boolean isWorkingDay = workingDays.stream().anyMatch(d -> d.equals(date));

        // Get leaves for this specific date
        List<LeaveRequest> leavesOnDate = leaveRequestRepository.findApprovedLeavesForDeptSince(deptId, date)
                .stream()
                .filter(lr -> !date.isBefore(lr.getFromDate()) && !date.isAfter(lr.getToDate()))
                .toList();

        Map<Long, LeaveRequest> leaveByFaculty = leavesOnDate.stream()
                .collect(java.util.stream.Collectors.toMap(
                        lr -> lr.getFaculty().getId(),
                        lr -> lr,
                        (a, b) -> a // keep first if duplicate
                ));

        List<DailyAttendanceStat> result = new ArrayList<>();
        for (Faculty f : faculties) {
            String status;
            if (!isWorkingDay) {
                status = "NON_WORKING_DAY";
            } else {
                LeaveRequest leave = leaveByFaculty.get(f.getId());
                if (leave == null) {
                    status = "PRESENT";
                } else if (leave.getType() == LeaveType.ON_DUTY) {
                    status = "ON_DUTY";
                } else {
                    status = "ON_LEAVE";
                }
            }
            result.add(DailyAttendanceStat.builder()
                    .facultyId(f.getId())
                    .facultyName(f.getName())
                    .rollNumber(f.getRollNumber())
                    .status(status)
                    .build());
        }
        return result;
    }
}
