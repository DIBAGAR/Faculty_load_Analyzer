package com.abc.facultyload.controller;

import com.abc.facultyload.dto.request.*;
import com.abc.facultyload.entity.*;
import com.abc.facultyload.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
public class HodController {

    private final HodService hodService;
    private final TimetableService timetableService;
    private final WorkAssignmentService workAssignmentService;
    private final LeaveService leaveService;
    private final FacultyService facultyService;
    private final com.abc.facultyload.repository.WorkAssignmentRepository workAssignmentRepository;

    // === Dashboard ===
    @GetMapping("/dashboard/{deptId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(hodService.getDeptDashboard(deptId));
    }

    @GetMapping("/performance/{deptId}")
    public ResponseEntity<Map<String, Object>> getPerformance(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(hodService.getPerformanceStats(deptId));
    }

    // === Sections ===
    @GetMapping("/sections/{deptId}")
    public ResponseEntity<List<Section>> getSections(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(timetableService.getSectionsByDept(deptId));
    }

    @PostMapping("/sections")
    public ResponseEntity<Section> createSection(@RequestBody Map<String, Object> payload) {
        Long deptId = Long.parseLong(payload.get("deptId").toString());
        Integer year = Integer.parseInt(payload.get("year").toString());
        Integer semester = Integer.parseInt(payload.get("semester").toString());
        String sectionName = (String) payload.get("sectionName");
        return ResponseEntity.ok(timetableService.createSection(deptId, year, semester, sectionName));
    }

    // === Timetables ===
    @GetMapping("/timetables/section/{sectionId}")
    public ResponseEntity<List<Timetable>> getTimetablesBySection(@PathVariable("sectionId") Long sectionId) {
        return ResponseEntity.ok(timetableService.getTimetablesBySection(sectionId));
    }



    @PostMapping("/timetables/{id}/slots")
    public ResponseEntity<Void> saveSlots(@PathVariable("id") Long id, @RequestBody List<TimetableSlotRequest> slots) {
        timetableService.saveTimetableSlots(id, slots);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/timetables/{id}/slots")
    public ResponseEntity<List<TimetableSlot>> getSlots(@PathVariable("id") Long id) {
        return ResponseEntity.ok(timetableService.getSlotsByTimetable(id));
    }

    @GetMapping("/timetables/occupied-venues/{deptId}")
    public ResponseEntity<Map<String, List<Long>>> getOccupiedVenues(
            @PathVariable("deptId") Long deptId,
            @RequestParam(value = "currentTimetableId", required = false) Long currentTimetableId) {
        return ResponseEntity.ok(timetableService.getOccupiedVenuesMatrix(deptId, currentTimetableId));
    }

    @PutMapping("/timetables/{id}/status")
    public ResponseEntity<Timetable> setStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(timetableService.setTimetableStatus(id, Timetable.TimetableStatus.valueOf(payload.get("status"))));
    }

    @PostMapping("/timetables/{id}/copy-to-section")
    public ResponseEntity<Timetable> copyToNewSection(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload) {
        Long deptId = Long.parseLong(payload.get("deptId").toString());
        Integer year = Integer.parseInt(payload.get("year").toString());
        Integer semester = Integer.parseInt(payload.get("semester").toString());
        String sectionName = (String) payload.get("sectionName");
        return ResponseEntity.ok(timetableService.copyToNewSection(id, deptId, year, semester, sectionName));
    }

    @DeleteMapping("/timetables/{id}")
    public ResponseEntity<Void> deleteTimetable(@PathVariable("id") Long id) {
        timetableService.deleteTimetable(id);
        return ResponseEntity.ok().build();
    }

    // === Work Generation ===
    @PostMapping("/work/generate")
    public ResponseEntity<?> generateWork(@RequestBody WorkGenerationRequest req) {
        return ResponseEntity.ok(workAssignmentService.generateWork(req.getDeptId(), req.getStartDate(), req.getEndDate(), req.getStartHour(), req.getEndHour()));
    }

    @GetMapping("/work/view")
    public ResponseEntity<?> viewWork(
            @RequestParam("deptId") Long deptId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "facultyId", required = false) Long facultyId) {
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        List<?> assignments = facultyId != null
                ? workAssignmentRepository.findByDeptAndFacultyAndDateRange(deptId, facultyId, start, end)
                : workAssignmentRepository.findByDeptAndDateRange(deptId, start, end);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/work/manual-assign")
    public ResponseEntity<?> manualAssign(@RequestBody com.abc.facultyload.dto.request.ManualWorkAssignmentRequest req) {
        workAssignmentService.createManualAssignment(req);
        return ResponseEntity.ok(Map.of("message", "Work assigned successfully"));
    }

    @DeleteMapping("/work/remove")
    public ResponseEntity<?> removeWork(@RequestBody WorkGenerationRequest req) {
        workAssignmentService.deleteWorkInRange(req.getDeptId(), req.getStartDate(), req.getEndDate(), req.getStartHour(), req.getEndHour());
        return ResponseEntity.ok(Map.of("message", "Work removed successfully"));
    }

    @DeleteMapping("/work/delete")
    public ResponseEntity<Void> deleteWork(@RequestBody Map<String, Object> payload) {
        Long deptId = Long.parseLong(payload.get("deptId").toString());
        LocalDate start = LocalDate.parse(payload.get("startDate").toString());
        LocalDate end = LocalDate.parse(payload.get("endDate").toString());
        workAssignmentService.deleteWorkInRange(deptId, start, end, 1, 7);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/faculty/dept/{deptId}")
    public ResponseEntity<List<com.abc.facultyload.dto.response.FacultyResponse>> getDeptFaculty(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(facultyService.getFacultyByDepartment(deptId));
    }

    // === Course-Faculty Mapping ===
    @GetMapping("/mappings/{deptId}")
    public ResponseEntity<List<CourseFacultyMapping>> getMappings(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(hodService.getMappingsByDept(deptId));
    }

    @PostMapping("/mappings")
    public ResponseEntity<CourseFacultyMapping> addMapping(@RequestBody Map<String, Object> payload) {
        Long facultyId = Long.parseLong(payload.get("facultyId").toString());
        Long courseId = Long.parseLong(payload.get("courseId").toString());
        String type = (String) payload.get("type");
        return ResponseEntity.ok(hodService.addMapping(facultyId, courseId, type));
    }

    @DeleteMapping("/mappings/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable("id") Long id) {
        hodService.removeMapping(id);
        return ResponseEntity.ok().build();
    }

    // === Leave Approval (for faculty leave) ===
    @GetMapping("/leaves/{deptId}")
    public ResponseEntity<List<LeaveRequest>> getLeaves(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(leaveService.getDeptLeaveRequests(deptId));
    }

    @GetMapping("/leaves/pending/{deptId}")
    public ResponseEntity<List<LeaveRequest>> getPendingLeavesForHod(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(leaveService.getPendingLeavesForHod(deptId));
    }

    @GetMapping("/leaves/pending-admin")
    public ResponseEntity<List<LeaveRequest>> getPendingLeavesForAdmin() {
        return ResponseEntity.ok(leaveService.getPendingLeavesForAdmin());
    }

    @GetMapping("/leaves/history/{deptId}")
    public ResponseEntity<List<LeaveRequest>> getHistoryLeavesForHod(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(leaveService.getHistoryLeavesForHod(deptId));
    }

    @GetMapping("/leaves/history-admin")
    public ResponseEntity<List<LeaveRequest>> getHistoryLeavesForAdmin() {
        return ResponseEntity.ok(leaveService.getHistoryLeavesForAdmin());
    }

    @PutMapping("/leaves/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveLeave(@PathVariable("id") Long id) {
        return ResponseEntity.ok(leaveService.approveLeave(id));
    }

    @PutMapping("/leaves/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(leaveService.rejectLeave(id, payload.get("reason")));
    }

    // === Occupied Faculty Matrix ===
    @GetMapping("/timetables/occupied-faculty/{deptId}")
    public ResponseEntity<Map<String, List<Long>>> getOccupiedFaculty(
            @PathVariable("deptId") Long deptId,
            @RequestParam(value = "currentTimetableId", required = false) Long currentTimetableId) {
        return ResponseEntity.ok(timetableService.getOccupiedFacultyMatrix(deptId, currentTimetableId));
    }

    // === Lab-Course Mapping CRUD ===
    private final com.abc.facultyload.repository.LabCourseMappingRepository labCourseMappingRepository;
    private final com.abc.facultyload.repository.VenueRepository venueRepository;
    private final com.abc.facultyload.repository.CourseRepository courseRepository;

    @GetMapping("/lab-mappings/{deptId}")
    public ResponseEntity<List<com.abc.facultyload.entity.LabCourseMapping>> getLabMappings(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(labCourseMappingRepository.findAllByDeptId(deptId));
    }

    @PostMapping("/lab-mappings")
    public ResponseEntity<?> addLabMapping(@RequestBody Map<String, Long> payload) {
        Long venueId = payload.get("venueId");
        Long courseId = payload.get("courseId");
        if (labCourseMappingRepository.existsByVenueIdAndCourseId(venueId, courseId))
            return ResponseEntity.badRequest().body("Mapping already exists");
        com.abc.facultyload.entity.Venue venue = venueRepository.findById(venueId).orElseThrow();
        com.abc.facultyload.entity.Course course = courseRepository.findById(courseId).orElseThrow();
        if (venue.getVenueType() != com.abc.facultyload.entity.Venue.VenueType.LAB)
            return ResponseEntity.badRequest().body("Venue must be a LAB type");
        com.abc.facultyload.entity.LabCourseMapping mapping = com.abc.facultyload.entity.LabCourseMapping.builder()
                .venue(venue).course(course).build();
        return ResponseEntity.ok(labCourseMappingRepository.save(mapping));
    }

    @DeleteMapping("/lab-mappings/{id}")
    public ResponseEntity<Void> deleteLabMapping(@PathVariable("id") Long id) {
        labCourseMappingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
