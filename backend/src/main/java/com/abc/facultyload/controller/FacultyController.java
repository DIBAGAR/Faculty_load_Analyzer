package com.abc.facultyload.controller;

import com.abc.facultyload.dto.request.LeaveRequestDto;
import com.abc.facultyload.entity.*;
import com.abc.facultyload.repository.FacultyRepository;
import com.abc.facultyload.repository.UserRepository;
import com.abc.facultyload.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;
    private final LeaveService leaveService;
    private final WorkAssignmentService workAssignmentService;
    private final TimetableService timetableService;
    private final HodService hodService;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;

    private Faculty getCurrentFaculty(Authentication auth) {
        String login = auth.getName();
        return userRepository.findByEmailOrRollNumber(login)
                .flatMap(u -> facultyRepository.findByUserId(u.getId()))
                .orElseThrow(() -> new RuntimeException("Faculty profile not found"));
    }

    // Faculty Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication auth) {
        Faculty faculty = getCurrentFaculty(auth);
        
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate startDate = startOfWeek.minusWeeks(5);
        LocalDate endDate = startOfWeek.plusWeeks(5).minusDays(1); // 10 weeks of data (-5 to +5)

        List<WorkAssignment> history = workAssignmentService.getFacultyWorkHistory(faculty.getId(), startDate, endDate);
        Map<String, Object> deptData = hodService.getDeptDashboard(faculty.getDepartment().getId());
        List<Timetable> deptTimetables = timetableService.getTimetablesByDept(faculty.getDepartment().getId());

        return ResponseEntity.ok(Map.of(
                "faculty", facultyService.toResponse(faculty),
                "workHistory", history,
                "deptTimetables", deptTimetables,
                "deptDashboard", deptData
        ));
    }

    // Apply Leave
    @PostMapping("/leaves")
    public ResponseEntity<LeaveRequest> applyLeave(Authentication auth, @RequestBody LeaveRequestDto dto) {
        Faculty faculty = getCurrentFaculty(auth);
        return ResponseEntity.ok(leaveService.applyLeave(faculty.getId(), dto));
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequest>> getLeaveHistory(Authentication auth) {
        Faculty faculty = getCurrentFaculty(auth);
        return ResponseEntity.ok(leaveService.getFacultyLeaveHistory(faculty.getId()));
    }

    // Cancel Leave
    @DeleteMapping("/leaves/{id}")
    public ResponseEntity<Map<String, String>> cancelLeave(Authentication auth, @PathVariable("id") Long id) {
        Faculty faculty = getCurrentFaculty(auth);
        leaveService.cancelLeave(id, faculty.getId());
        return ResponseEntity.ok(Map.of("message", "Leave request cancelled successfully"));
    }

    // Work History
    @GetMapping("/work-history")
    public ResponseEntity<List<WorkAssignment>> getWorkHistory(
            Authentication auth,
            @RequestParam(value="from", defaultValue = "") String from,
            @RequestParam(value="to", defaultValue = "") String to) {
        Faculty faculty = getCurrentFaculty(auth);
        LocalDate startDate = from.isBlank() ? LocalDate.now().minusWeeks(4) : LocalDate.parse(from);
        LocalDate endDate = to.isBlank() ? LocalDate.now() : LocalDate.parse(to);
        return ResponseEntity.ok(workAssignmentService.getFacultyWorkHistory(faculty.getId(), startDate, endDate));
    }

    // Get faculty list for a dept (for timetable dropdowns)
    @GetMapping("/dept/{deptId}")
    public ResponseEntity<?> getFacultyByDept(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(facultyService.getFacultyByDepartment(deptId));
    }

    // View Department Timetables
    @GetMapping("/dept-timetables")
    public ResponseEntity<List<Timetable>> getDeptTimetables(Authentication auth) {
        Faculty faculty = getCurrentFaculty(auth);
        return ResponseEntity.ok(timetableService.getTimetablesByDept(faculty.getDepartment().getId()));
    }

    @GetMapping("/timetables/{id}/slots")
    public ResponseEntity<List<TimetableSlot>> getTimetableSlots(@PathVariable("id") Long id) {
        return ResponseEntity.ok(timetableService.getSlotsByTimetable(id));
    }
}
