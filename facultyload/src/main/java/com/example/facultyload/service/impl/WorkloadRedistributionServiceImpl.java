package com.example.facultyload.service.impl;

import com.example.facultyload.entity.*;
import com.example.facultyload.repository.AssignedWorkRepository;
import com.example.facultyload.repository.FacultyCourseRepository;
import com.example.facultyload.repository.LeaveRequestRepository;
import com.example.facultyload.service.NotificationService;
import com.example.facultyload.service.WorkloadRedistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkloadRedistributionServiceImpl implements WorkloadRedistributionService {

    private final AssignedWorkRepository assignedWorkRepository;
    private final FacultyCourseRepository facultyCourseRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void redistributeForEmergencyLeave(LeaveRequest leaveRequest) {
        Faculty onLeave = leaveRequest.getFaculty();
        List<AssignedWork> impacted = assignedWorkRepository.findAssignmentsForLeaveWindow(
                onLeave.getId(),
                leaveRequest.getFromDate(),
                leaveRequest.getToDate(),
                leaveRequest.getHourNumber()
        );

        for (AssignedWork aw : impacted) {
            TimetableEntry te = aw.getTimetableEntry();
            Faculty replacement = chooseReplacement(
                    onLeave.getDepartment().getId(),
                    te.getCourse().getId(),
                    aw.getWorkDate(),
                    te.getDayOfWeek(),
                    te.getHourNumber(),
                    onLeave.getId()
            );

            if (replacement == null) {
                Faculty hod = onLeave.getDepartment().getHod();
                if (hod != null) {
                    notificationService.notifyUser(hod.getUser().getId(),
                            "Emergency leave redistribution failed: no replacement for course " + te.getCourse().getCourseCode()
                                    + " on " + te.getDayOfWeek() + " hour " + te.getHourNumber()
                                    + " (" + aw.getWorkDate() + ")");
                }
                continue;
            }

            onLeave.setMonthlyAssignedHours(Math.max(0, onLeave.getMonthlyAssignedHours() - 1));
            onLeave.setTotalAssignedHours(Math.max(0, onLeave.getTotalAssignedHours() - 1));

            replacement.setMonthlyAssignedHours(replacement.getMonthlyAssignedHours() + 1);
            replacement.setTotalAssignedHours(replacement.getTotalAssignedHours() + 1);

            aw.setFaculty(replacement);
            aw.setStatus("REDISTRIBUTED");
            assignedWorkRepository.save(aw);

            notificationService.notifyUser(replacement.getUser().getId(),
                    "You have been reassigned a class for " + te.getCourse().getCourseCode()
                            + " on " + te.getDayOfWeek() + " hour " + te.getHourNumber()
                            + " (" + aw.getWorkDate() + ")");
        }
    }

    private boolean isOnLeave(Long facultyId, java.time.LocalDate date, Integer hourNumber) {
        return !leaveRequestRepository.findApprovedLeaveCovering(facultyId, LeaveStatus.APPROVED, date, hourNumber).isEmpty();
    }

    private Faculty chooseReplacement(Long departmentId, Long courseId, java.time.LocalDate workDate, java.time.DayOfWeek dayOfWeek,
                                     Integer hourNumber, Long excludeFacultyId) {
        Faculty f = chooseByKnowledge(departmentId, courseId, CourseKnowledgeType.PRIMARY, workDate, dayOfWeek, hourNumber, excludeFacultyId);
        if (f != null) return f;
        return chooseByKnowledge(departmentId, courseId, CourseKnowledgeType.ADDITIONAL, workDate, dayOfWeek, hourNumber, excludeFacultyId);
    }

    private Faculty chooseByKnowledge(Long departmentId, Long courseId, CourseKnowledgeType type,
                                     java.time.LocalDate workDate, java.time.DayOfWeek dayOfWeek, Integer hourNumber, Long excludeFacultyId) {
        List<Faculty> candidates = facultyCourseRepository.findCandidates(departmentId, courseId, type);
        for (Faculty c : candidates) {
            if (c.getId().equals(excludeFacultyId)) continue;
            if (isOnLeave(c.getId(), workDate, hourNumber)) continue;
            if (assignedWorkRepository.existsByFacultyIdAndWorkDateAndTimetableEntry_DayOfWeekAndTimetableEntry_HourNumber(
                    c.getId(), workDate, dayOfWeek, hourNumber)) continue;
            return c;
        }
        return null;
    }
}

