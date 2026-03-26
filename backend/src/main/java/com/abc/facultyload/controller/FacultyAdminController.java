package com.abc.facultyload.controller;

import com.abc.facultyload.dto.request.FacultyRequest;
import com.abc.facultyload.dto.response.FacultyResponse;
import com.abc.facultyload.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty-admin")
@RequiredArgsConstructor
public class FacultyAdminController {

    private final FacultyService facultyService;

    @GetMapping("/faculties")
    public ResponseEntity<List<FacultyResponse>> getAllFaculty() {
        return ResponseEntity.ok(facultyService.getAllFaculty());
    }

    @PostMapping("/faculties")
    public ResponseEntity<FacultyResponse> createFaculty(@Valid @RequestBody FacultyRequest req) {
        return ResponseEntity.ok(facultyService.createFaculty(req));
    }

    @PutMapping("/faculties/{id}")
    public ResponseEntity<FacultyResponse> updateFaculty(@PathVariable("id") Long id, @Valid @RequestBody FacultyRequest req) {
        return ResponseEntity.ok(facultyService.updateFaculty(id, req));
    }

    @DeleteMapping("/faculties/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable("id") Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/faculties/export")
    public ResponseEntity<InputStreamResource> exportFacultyExcel() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=faculty_list.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(facultyService.exportFacultyExcel()));
    }
}
