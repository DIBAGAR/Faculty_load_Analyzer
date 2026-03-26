package com.abc.facultyload.controller;

import com.abc.facultyload.entity.Venue;
import com.abc.facultyload.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venue-admin")
@RequiredArgsConstructor
public class VenueAdminController {

    private final VenueService venueService;

    @GetMapping("/venues")
    public ResponseEntity<List<Venue>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @PostMapping("/venues")
    public ResponseEntity<Venue> createVenue(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(venueService.createVenue(payload));
    }

    @PutMapping("/venues/{id}")
    public ResponseEntity<Venue> updateVenue(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(venueService.updateVenue(id, payload));
    }

    @DeleteMapping("/venues/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable("id") Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/venues/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=venue_list.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(venueService.exportVenuesExcel()));
    }

    @GetMapping("/venues/dept/{deptId}")
    public ResponseEntity<List<Venue>> getVenuesByDept(@PathVariable("deptId") Long deptId) {
        return ResponseEntity.ok(venueService.getVenuesByDept(deptId));
    }
}
