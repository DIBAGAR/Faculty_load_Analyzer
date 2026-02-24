package com.example.facultyload.controller;

import com.example.facultyload.dto.timetable.TimetableCreateRequest;
import com.example.facultyload.dto.timetable.TimetableResponse;
import com.example.facultyload.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timetables")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PreAuthorize("hasRole('HOD') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public TimetableResponse createDraft(@Valid @RequestBody TimetableCreateRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return timetableService.createDraft(request, email);
    }

    @PreAuthorize("hasRole('HOD') or hasRole('SUPER_ADMIN')")
    @PostMapping("/{timetableId}/activate")
    public TimetableResponse activate(@PathVariable Long timetableId) {
        return timetableService.activate(timetableId);
    }

    @GetMapping("/active")
    public TimetableResponse getActive(@RequestParam Long departmentId, @RequestParam Integer yearOfStudy, @RequestParam String section) {
        return timetableService.getActive(departmentId, yearOfStudy, section);
    }
}

