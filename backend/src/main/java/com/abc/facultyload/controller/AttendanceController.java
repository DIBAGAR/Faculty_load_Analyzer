package com.abc.facultyload.controller;

import com.abc.facultyload.service.AttendanceService;
import com.abc.facultyload.service.AttendanceService.FacultyAttendanceStat;
import com.abc.facultyload.service.AttendanceService.DailyAttendanceStat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hod/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * HOD: Get all faculty attendance stats for their department
     */
    @GetMapping("/{deptId}")
    public ResponseEntity<List<FacultyAttendanceStat>> getDeptAttendanceStats(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(attendanceService.getDeptAttendanceStats(deptId));
    }

    /**
     * HOD: Get daily present/absent status for a specific date
     */
    @GetMapping("/{deptId}/daily")
    public ResponseEntity<List<DailyAttendanceStat>> getDailyAttendance(
            @PathVariable("deptId") Long deptId,
            @RequestParam("date") String date) {
        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(attendanceService.getDailyAttendance(deptId, localDate));
    }
}
