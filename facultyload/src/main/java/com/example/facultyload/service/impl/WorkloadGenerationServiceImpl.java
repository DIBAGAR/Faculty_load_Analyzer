package com.example.facultyload.service.impl;

import com.example.facultyload.dto.workload.WorkloadGenerationResponse;
import com.example.facultyload.entity.*;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.exception.ForbiddenException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.*;
import com.example.facultyload.service.NotificationService;
import com.example.facultyload.service.WorkloadGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkloadGenerationServiceImpl implements WorkloadGenerationService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final FacultyCourseRepository facultyCourseRepository;
    private final AssignedWorkRepository assignedWorkRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public WorkloadGenerationResponse generateNextWeekWork(Long departmentId, Integer yearOfStudy, String section, String requesterEmail) {
        Faculty requester = facultyRepository.findByUserEmailIgnoreCase(requesterEmail)
                .orElseThrow(() -> new NotFoundException("Faculty not found for user: " + requesterEmail));
        if (!requester.isHod() || !requester.getDepartment().getId().equals(departmentId)) {
            throw new ForbiddenException("Only HOD of the department can generate workload");
        }

        Timetable active = timetableRepository.findByDepartmentIdAndYearOfStudyAndSectionAndIsActiveTrue(departmentId, yearOfStudy, section.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Active timetable not found for department/year/section"));

        List<TimetableEntry> entries = timetableEntryRepository.findByTimetableId(active.getId());
        if (entries.isEmpty()) {
            throw new BadRequestException("Active timetable has no entries");
        }

        LocalDate weekStart = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));

        int created = 0;
        int unassigned = 0;
        Set<Long> touchedFacultyIds = new HashSet<>();
        List<AssignedWork> toInsert = new ArrayList<>();

        for (TimetableEntry te : entries) {
            if (te.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            LocalDate workDate = weekStart.plusDays(te.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());

            Faculty assigned = te.getDefaultFaculty();
            boolean needsReassign = (assigned == null) || isOnLeave(assigned.getId(), workDate, te.getHourNumber());
            if (!needsReassign) {
                // avoid double-booking the same faculty for the same hour
                if (assignedWorkRepository.existsByFacultyIdAndWorkDateAndTimetableEntry_DayOfWeekAndTimetableEntry_HourNumber(
                        assigned.getId(), workDate, te.getDayOfWeek(), te.getHourNumber())) {
                    needsReassign = true;
                }
            }

            if (needsReassign) {
                assigned = chooseReplacement(departmentId, te.getCourse().getId(), workDate, te.getDayOfWeek(), te.getHourNumber());
            }

            if (assigned == null) {
                unassigned++;
                Faculty hod = department.getHod();
                if (hod != null) {
                    notificationService.notifyUser(hod.getUser().getId(),
                            "No available faculty found for course " + te.getCourse().getCourseCode()
                                    + " on " + te.getDayOfWeek() + " hour " + te.getHourNumber()
                                    + " (week starting " + weekStart + ")");
                }
                continue;
            }

            AssignedWork aw = AssignedWork.builder()
                    .faculty(assigned)
                    .timetableEntry(te)
                    .workDate(workDate)
                    .weekStartDate(weekStart)
                    .status("ASSIGNED")
                    .build();
            toInsert.add(aw);

            assigned.setMonthlyAssignedHours(assigned.getMonthlyAssignedHours() + 1);
            assigned.setTotalAssignedHours(assigned.getTotalAssignedHours() + 1);
            touchedFacultyIds.add(assigned.getId());
            created++;
        }

        try {
            assignedWorkRepository.saveAll(toInsert);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Work for next week appears to have already been generated");
        }

        // Keep only last 4 weeks per faculty (including the newly generated week)
        LocalDate cutoff = weekStart.minusWeeks(3);
        for (Long fid : touchedFacultyIds) {
            assignedWorkRepository.deleteByFacultyIdAndWeekStartDateBefore(fid, cutoff);
        }

        return new WorkloadGenerationResponse(weekStart, created, unassigned);
    }

    private boolean isOnLeave(Long facultyId, LocalDate date, Integer hourNumber) {
        return !leaveRequestRepository.findApprovedLeaveCovering(facultyId, LeaveStatus.APPROVED, date, hourNumber).isEmpty();
    }

    private Faculty chooseReplacement(Long departmentId, Long courseId, LocalDate workDate, DayOfWeek dayOfWeek, Integer hourNumber) {
        Faculty f = chooseByKnowledge(departmentId, courseId, CourseKnowledgeType.PRIMARY, workDate, dayOfWeek, hourNumber);
        if (f != null) return f;
        return chooseByKnowledge(departmentId, courseId, CourseKnowledgeType.ADDITIONAL, workDate, dayOfWeek, hourNumber);
    }

    private Faculty chooseByKnowledge(Long departmentId, Long courseId, CourseKnowledgeType type,
                                     LocalDate workDate, DayOfWeek dayOfWeek, Integer hourNumber) {
        List<Faculty> candidates = facultyCourseRepository.findCandidates(departmentId, courseId, type);
        for (Faculty c : candidates) {
            if (isOnLeave(c.getId(), workDate, hourNumber)) continue;
            if (assignedWorkRepository.existsByFacultyIdAndWorkDateAndTimetableEntry_DayOfWeekAndTimetableEntry_HourNumber(
                    c.getId(), workDate, dayOfWeek, hourNumber)) continue;
            return c;
        }
        return null;
    }
}

