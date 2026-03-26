package com.abc.facultyload.service;

import com.abc.facultyload.entity.*;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkAssignmentService {

    private final TimetableRepository timetableRepository;
    private final TimetableSlotRepository slotRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final FacultyMonthlyLoadRepository monthlyLoadRepository;
    private final CourseFacultyMappingRepository mappingRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final FacultyRepository facultyRepository;
    private final NotificationService notificationService;
    private final CourseRepository courseRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public Map<String, Object> generateWork(Long deptId, LocalDate startDate, LocalDate endDate, Integer startH, Integer endH) {
        if (startDate.until(endDate).getDays() > 15)
            throw new AppException("Date range cannot exceed 15 days", HttpStatus.BAD_REQUEST);

        int startHour = (startH != null) ? startH : 1;
        int endHour = (endH != null) ? endH : 7;

        if (workAssignmentRepository.existsInRange(deptId, startDate, endDate, startHour, endHour))
            throw new AppException("Work already assigned in this date/hour range for this department", HttpStatus.CONFLICT);

        List<Timetable> activeTimetables = timetableRepository.findActiveTimetablesByDeptId(deptId);
        if (activeTimetables.isEmpty())
            throw new AppException("No active timetables found for department", HttpStatus.BAD_REQUEST);

        int assigned = 0;
        int skipped = 0;
        List<Map<String, Object>> skippedDetails = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                current = current.plusDays(1);
                continue;
            }

            int dayOfWeek = current.getDayOfWeek().getValue(); // 1=Mon ... 6=Sat

            for (Timetable timetable : activeTimetables) {
                List<TimetableSlot> slots = slotRepository.findAllByTimetableId(timetable.getId());

                for (TimetableSlot slot : slots) {
                    if (slot.getDayOfWeek() == null || !slot.getDayOfWeek().equals(dayOfWeek)) continue;
                    if (slot.getHour() < startHour || slot.getHour() > endHour) continue;
                    if (slot.getCourse() == null) continue;

                    Faculty selected = selectFaculty(slot, current, slot.getHour(), false);

                    if (selected == null) {
                        notifyHodNoFaculty(deptId, current, slot);
                        skipped++;
                        addSkippedDetail(skippedDetails, current, slot, "PRIMARY", deptId);
                    } else if (slot.getVenue() != null && workAssignmentRepository.venueConflictsAt(slot.getVenue().getId(), current, slot.getHour())) {
                        notifyHodNoFaculty(deptId, current, slot);
                        skipped++;
                        addSkippedDetail(skippedDetails, current, slot, "VENUE CONFLICT", deptId);
                    } else {
                        WorkAssignment wa = WorkAssignment.builder()
                                .faculty(selected)
                                .timetableSlot(slot)
                                .course(slot.getCourse())
                                .slotType(slot.getSlotType())
                                .assignDate(current)
                                .hour(slot.getHour())
                                .venue(slot.getVenue())
                                .build();
                        workAssignmentRepository.save(wa);
                        incrementLoad(selected, current);
                        assigned++;
                    }

                    // For LAB slots, handle optional additional faculty
                    if (slot.getSlotType() == TimetableSlot.SlotType.LAB && slot.getAdditionalFaculty() != null) {
                        Faculty addFaculty = selectFaculty(slot, current, slot.getHour(), true);
                        if (addFaculty == null) {
                            skipped++;
                            addSkippedDetail(skippedDetails, current, slot, "ADDITIONAL", deptId);
                        } else {
                            // If primary didn't have a venue conflict, additional won't either (it's the same venue)
                            WorkAssignment waAdd = WorkAssignment.builder()
                                    .faculty(addFaculty)
                                    .timetableSlot(slot)
                                    .course(slot.getCourse())
                                    .slotType(slot.getSlotType())
                                    .assignDate(current)
                                    .hour(slot.getHour())
                                    .venue(slot.getVenue())
                                    .build();
                            workAssignmentRepository.save(waAdd);
                            incrementLoad(addFaculty, current);
                            assigned++;
                        }
                    }
                }
            }

            current = current.plusDays(1);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assigned", assigned);
        result.put("skipped", skipped);
        result.put("skippedDetails", skippedDetails);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        return result;
    }

    private void addSkippedDetail(List<Map<String, Object>> skippedDetails, LocalDate date, TimetableSlot slot, String missingRole, Long deptId) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("date", date.toString());
        detail.put("hour", slot.getHour());
        detail.put("courseId", slot.getCourse().getId());
        detail.put("courseName", slot.getCourse().getCourseName());
        detail.put("courseCode", slot.getCourse().getCourseCode());
        detail.put("venueId", slot.getVenue() != null ? slot.getVenue().getId() : null);
        detail.put("venueName", slot.getVenue() != null ? slot.getVenue().getVenueName() : null);
        detail.put("timetableSlotId", slot.getId());
        detail.put("timetableName", slot.getTimetable() != null ? slot.getTimetable().getTimetableLabel() : null);
        detail.put("sectionName", slot.getTimetable() != null && slot.getTimetable().getSection() != null ? slot.getTimetable().getSection().getSectionName() : null);
        detail.put("slotType", slot.getSlotType().name());
        detail.put("missingRole", missingRole);
        // ALL free dept faculty ordered by least load (not just course-mapped)
        List<Map<String, Object>> availFaculty = buildDeptAvailableFacultyList(deptId, date, slot.getHour());
        detail.put("availableFaculty", availFaculty);
        skippedDetails.add(detail);
    }

    /**
     * Returns all active non-HOD faculty in the department who are free on that date+hour,
     * ordered by monthly load ascending (least busy first).
     */
    private List<Map<String, Object>> buildDeptAvailableFacultyList(Long deptId, LocalDate date, Integer hour) {
        return facultyRepository.findAllByDepartmentIdAndActiveTrue(deptId).stream()
            .filter(f -> f.getUser().getRole().getName() != Role.RoleName.HOD)
            .filter(f -> isAvailable(f, date, hour))
            .sorted(Comparator.comparingInt(f -> getCurrentMonthLoad((Faculty) f, date)))
            .map(f -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("id", f.getId()); m.put("name", f.getName()); m.put("load", getCurrentMonthLoad(f, date)); return m; })
            .toList();
    }

    private Faculty selectFaculty(TimetableSlot slot, LocalDate date, Integer hour, boolean isAdditional) {
        if (!isAdditional) {
            if (slot.getDefaultFaculty() != null && isAvailable(slot.getDefaultFaculty(), date, hour)) {
                return slot.getDefaultFaculty();
            }
        } else {
            if (slot.getAdditionalFaculty() != null && isAvailable(slot.getAdditionalFaculty(), date, hour)) {
                return slot.getAdditionalFaculty();
            }
        }

        // Priority 2: Primary course-known faculty
        List<CourseFacultyMapping> primaryMappings = mappingRepository.findByCourseIdAndType(
                slot.getCourse().getId(), CourseFacultyMapping.MappingType.PRIMARY);
        Faculty primary = pickLeastLoadedAvailable(primaryMappings.stream().map(CourseFacultyMapping::getFaculty).toList(), date, hour);
        if (primary != null) return primary;

        // Priority 3: Additional course-known faculty
        List<CourseFacultyMapping> additionalMappings = mappingRepository.findByCourseIdAndType(
                slot.getCourse().getId(), CourseFacultyMapping.MappingType.ADDITIONAL);
        Faculty additional = pickLeastLoadedAvailable(additionalMappings.stream().map(CourseFacultyMapping::getFaculty).toList(), date, hour);
        if (additional != null) return additional;

        // No course-known faculty available — return null (HOD must assign manually)
        return null;
    }

    private Faculty pickLeastLoadedAvailable(List<Faculty> candidates, LocalDate date, Integer hour) {
        return candidates.stream()
                .filter(f -> f.getUser().getRole().getName() != Role.RoleName.HOD)
                .filter(f -> isAvailable(f, date, hour))
                .min(Comparator.comparingInt(f -> getCurrentMonthLoad(f, date)))
                .orElse(null);
    }

    private boolean isAvailable(Faculty faculty, LocalDate date, Integer hour) {
        if (!faculty.isActive()) return false;
        if (leaveRequestRepository.hasApprovedLeaveInRange(faculty.getId(), date, date)) return false;
        return workAssignmentRepository.findByFacultyIdAndAssignDateAndHour(faculty.getId(), date, hour).isEmpty();
    }

    private int getCurrentMonthLoad(Faculty faculty, LocalDate date) {
        return monthlyLoadRepository.findByFacultyIdAndYearAndMonth(faculty.getId(), date.getYear(), date.getMonthValue())
                .map(FacultyMonthlyLoad::getTotalHours).orElse(0);
    }

    private void incrementLoad(Faculty faculty, LocalDate date) {
        FacultyMonthlyLoad load = monthlyLoadRepository
                .findByFacultyIdAndYearAndMonth(faculty.getId(), date.getYear(), date.getMonthValue())
                .orElse(FacultyMonthlyLoad.builder().faculty(faculty).year(date.getYear()).month(date.getMonthValue()).totalHours(0).build());
        load.setTotalHours(load.getTotalHours() + 1);
        monthlyLoadRepository.save(load);
    }

    @Transactional
    public void decrementLoad(Faculty faculty, LocalDate date) {
        monthlyLoadRepository.findByFacultyIdAndYearAndMonth(faculty.getId(), date.getYear(), date.getMonthValue())
                .ifPresent(load -> {
                    load.setTotalHours(Math.max(0, load.getTotalHours() - 1));
                    monthlyLoadRepository.save(load);
                });
    }

    /**
     * Reassign work for approved leave. Returns list of slots that could NOT be auto-reassigned
     * so the HOD can manually assign them via the popup.
     * Now synchronous (not @Async) so the caller gets the result.
     */
    @Transactional
    public List<Map<String, Object>> reassignWorkForLeave(Faculty faculty, LocalDate fromDate, LocalDate toDate) {
        List<WorkAssignment> assignments = workAssignmentRepository.findReassignableWork(faculty.getId(), fromDate, toDate);
        List<Map<String, Object>> unresolved = new ArrayList<>();

        for (WorkAssignment wa : assignments) {
            if (wa.getTimetableSlot() == null) {
                // No slot context — mark faculty null, add to unresolved
                decrementLoad(faculty, wa.getAssignDate());
                wa.setFaculty(null);
                workAssignmentRepository.save(wa);
                Map<String, Object> row = buildUnresolvedRow(wa, faculty);
                row.put("availableFaculty", List.of());
                unresolved.add(row);
                continue;
            }

            boolean isAdditional = wa.getTimetableSlot().getAdditionalFaculty() != null &&
                                   wa.getTimetableSlot().getAdditionalFaculty().getId().equals(faculty.getId());
            Faculty replacement = selectFaculty(wa.getTimetableSlot(), wa.getAssignDate(), wa.getHour(), isAdditional);

            if (replacement != null) {
                decrementLoad(faculty, wa.getAssignDate());
                wa.setFaculty(replacement);
                wa.setReassigned(true);
                workAssignmentRepository.save(wa);
                incrementLoad(replacement, wa.getAssignDate());
                notifyFacultyReassigned(faculty, replacement, wa);
            } else {
                // Cannot auto-reassign — expose to HOD for manual assignment
                Map<String, Object> row = buildUnresolvedRow(wa, faculty);
                Long deptFacultyListId = faculty.getDepartment().getId();
                List<Map<String, Object>> avail = buildDeptAvailableFacultyList(deptFacultyListId, wa.getAssignDate(), wa.getHour());
                row.put("availableFaculty", avail);
                unresolved.add(row);
                notifyHodNoFaculty(faculty.getDepartment().getId(), wa.getAssignDate(), wa.getHour(), wa.getCourse());
            }
        }
        return unresolved;
    }

    private Map<String, Object> buildUnresolvedRow(WorkAssignment wa, Faculty absentFaculty) {
        Map<String, Object> row = new HashMap<>();
        row.put("workAssignmentId", wa.getId());
        row.put("date", wa.getAssignDate().toString());
        row.put("hour", wa.getHour());
        row.put("courseId", wa.getCourse() != null ? wa.getCourse().getId() : null);
        row.put("courseCode", wa.getCourse() != null ? wa.getCourse().getCourseCode() : null);
        row.put("courseName", wa.getCourse() != null ? wa.getCourse().getCourseName() : null);
        row.put("venueId", wa.getVenue() != null ? wa.getVenue().getId() : null);
        row.put("venueName", wa.getVenue() != null ? wa.getVenue().getVenueName() : null);
        row.put("slotType", wa.getSlotType() != null ? wa.getSlotType().name() : "THEORY");
        row.put("timetableSlotId", wa.getTimetableSlot() != null ? wa.getTimetableSlot().getId() : null);
        row.put("timetableName", wa.getTimetableSlot() != null && wa.getTimetableSlot().getTimetable() != null ? wa.getTimetableSlot().getTimetable().getTimetableLabel() : null);
        row.put("sectionName", wa.getTimetableSlot() != null && wa.getTimetableSlot().getTimetable() != null && wa.getTimetableSlot().getTimetable().getSection() != null ? wa.getTimetableSlot().getTimetable().getSection().getSectionName() : null);
        row.put("absentFacultyName", absentFaculty.getName());
        return row;
    }

    private void notifyHodNoFaculty(Long deptId, LocalDate date, TimetableSlot slot) {
        notifyHodNoFaculty(deptId, date, slot.getHour(), slot.getCourse());
    }

    private void notifyHodNoFaculty(Long deptId, LocalDate date, Integer hour, Course course) {
        try {
            facultyRepository.findHodByDeptId(deptId, Role.RoleName.HOD).ifPresent(hod -> {
                String msg = String.format("No faculty available for %s on %s, Hour %d",
                        course != null ? course.getCourseName() : "Unknown", date, hour);
                notificationService.createAndSendNotification(hod.getUser(), "Work Assignment Alert", msg, true);
            });
        } catch (Exception e) {
            log.error("Failed to notify HOD: {}", e.getMessage());
        }
    }

    private void notifyFacultyReassigned(Faculty original, Faculty replacement, WorkAssignment wa) {
        try {
            String msg = String.format("Your work on %s Hour %d has been reassigned to %s due to leave.",
                    wa.getAssignDate(), wa.getHour(), replacement.getName());
            notificationService.createAndSendNotification(original.getUser(), "Work Reassigned", msg, true);

            String msg2 = String.format("You have been assigned to teach %s on %s Hour %d.",
                    wa.getCourse() != null ? wa.getCourse().getCourseName() : "Unknown",
                    wa.getAssignDate(), wa.getHour());
            notificationService.createAndSendNotification(replacement.getUser(), "New Work Assignment", msg2, true);
        } catch (Exception e) {
            log.error("Failed to notify faculty: {}", e.getMessage());
        }
    }

    @Transactional
    public void deleteWorkInRange(Long deptId, LocalDate startDate, LocalDate endDate, Integer startH, Integer endH) {
        int startHour = (startH != null) ? startH : 1;
        int endHour = (endH != null) ? endH : 7;
        
        List<WorkAssignment> tasksToDelete = workAssignmentRepository.findWorkToDelete(deptId, startDate, endDate, startHour, endHour);
        for (WorkAssignment wa : tasksToDelete) {
            decrementLoad(wa.getFaculty(), wa.getAssignDate());
            workAssignmentRepository.delete(wa);
        }
    }

    public List<WorkAssignment> getFacultyWorkHistory(Long facultyId, LocalDate from, LocalDate to) {
        return workAssignmentRepository.findAllByFacultyIdAndAssignDateBetweenOrderByAssignDateAsc(facultyId, from, to);
    }

    @Transactional
    public void createManualAssignment(com.abc.facultyload.dto.request.ManualWorkAssignmentRequest req) {
        Faculty faculty = facultyRepository.findById(req.getFacultyId())
                .orElseThrow(() -> new AppException("Faculty not found", HttpStatus.NOT_FOUND));
        Course course = req.getCourseId() != null ? courseRepository.findById(req.getCourseId()).orElse(null) : null;
        Venue venue = req.getVenueId() != null ? venueRepository.findById(req.getVenueId()).orElse(null) : null;
        TimetableSlot slot = req.getTimetableSlotId() != null ? slotRepository.findById(req.getTimetableSlotId()).orElse(null) : null;

        if (!isAvailable(faculty, req.getDate(), req.getHour())) {
            throw new AppException("Faculty is already assigned to work or on leave for this hour", HttpStatus.CONFLICT);
        }

        if (venue != null && workAssignmentRepository.venueConflictsAt(venue.getId(), req.getDate(), req.getHour())) {
            throw new AppException("Venue is already occupied at this time", HttpStatus.CONFLICT);
        }

        WorkAssignment wa = WorkAssignment.builder()
                .faculty(faculty)
                .timetableSlot(slot)
                .course(course)
                .slotType(req.getSlotType() != null ? TimetableSlot.SlotType.valueOf(req.getSlotType()) : TimetableSlot.SlotType.THEORY)
                .assignDate(req.getDate())
                .hour(req.getHour())
                .venue(venue)
                .build();
        workAssignmentRepository.save(wa);
        incrementLoad(faculty, req.getDate());
    }
}
