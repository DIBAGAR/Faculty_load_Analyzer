package com.abc.facultyload.service;

import com.abc.facultyload.entity.Department;
import com.abc.facultyload.entity.Venue;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.DepartmentRepository;
import com.abc.facultyload.repository.VenueRepository;
import com.abc.facultyload.util.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final DepartmentRepository departmentRepository;
    private final ExcelExporter excelExporter;

    public List<Venue> getAllVenues() {
        return venueRepository.findAllByActiveTrue();
    }

    public List<Venue> getVenuesByDept(Long deptId) {
        return venueRepository.findAllByDepartmentIdAndActiveTrue(deptId);
    }

    @Transactional
    public Venue createVenue(Map<String, Object> payload) {
        String venueName = (String) payload.get("venueName");
        if (venueRepository.existsByVenueName(venueName)) {
            throw new AppException("Venue name already exists", HttpStatus.BAD_REQUEST);
        }

        Department dept = null;
        if (payload.get("departmentId") != null) {
            dept = departmentRepository.findById(Long.parseLong(payload.get("departmentId").toString()))
                    .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));
        }

        Venue venue = Venue.builder()
                .block((String) payload.get("block"))
                .venueName(venueName)
                .venueType(Venue.VenueType.valueOf(payload.get("venueType").toString()))
                .department(dept)
                .capacity(payload.get("capacity") != null ? Integer.parseInt(payload.get("capacity").toString()) : null)
                .active(true)
                .build();
        return venueRepository.save(venue);
    }

    @Transactional
    public Venue updateVenue(Long id, Map<String, Object> payload) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new AppException("Venue not found", HttpStatus.NOT_FOUND));

        String newVenueName = (String) payload.get("venueName");
        if (!venue.getVenueName().equalsIgnoreCase(newVenueName) && venueRepository.existsByVenueName(newVenueName)) {
            throw new AppException("Venue name already exists", HttpStatus.BAD_REQUEST);
        }

        if (payload.get("departmentId") != null) {
            Department dept = departmentRepository.findById(Long.parseLong(payload.get("departmentId").toString()))
                    .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));
            venue.setDepartment(dept);
        }
        venue.setBlock((String) payload.get("block"));
        venue.setVenueName(newVenueName);
        venue.setVenueType(Venue.VenueType.valueOf(payload.get("venueType").toString()));
        if (payload.get("capacity") != null) venue.setCapacity(Integer.parseInt(payload.get("capacity").toString()));
        return venueRepository.save(venue);
    }

    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new AppException("Venue not found", HttpStatus.NOT_FOUND));
        venueRepository.delete(venue);
    }

    public ByteArrayInputStream exportVenuesExcel() {
        List<Venue> venues = venueRepository.findAllByActiveTrue();
        List<String> headers = List.of("Block", "Venue Name", "Type", "Department", "Capacity");
        List<Map<String, Object>> data = venues.stream().map(v -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Block", v.getBlock());
            row.put("Venue Name", v.getVenueName());
            row.put("Type", v.getVenueType().name());
            row.put("Department", v.getDepartment() != null ? v.getDepartment().getDeptName() : "");
            row.put("Capacity", v.getCapacity());
            return row;
        }).toList();
        return excelExporter.exportToExcel("Venue List", headers, data);
    }
}
