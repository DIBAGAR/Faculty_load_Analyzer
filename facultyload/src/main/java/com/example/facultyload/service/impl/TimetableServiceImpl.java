package com.example.facultyload.service.impl;

import com.example.facultyload.dto.timetable.*;
import com.example.facultyload.entity.*;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.*;
import com.example.facultyload.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableServiceImpl implements TimetableService {

    private final DepartmentRepository departmentRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final CourseRepository courseRepository;
    private final VenueRepository venueRepository;
    private final FacultyRepository facultyRepository;

    @Override
    @Transactional
    public TimetableResponse createDraft(TimetableCreateRequest request, String createdByEmail) {
        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + request.departmentId()));

        Faculty createdBy = (createdByEmail == null) ? null : facultyRepository.findByUserEmailIgnoreCase(createdByEmail).orElse(null);

        int nextVersion = timetableRepository
                .findByDepartmentIdAndYearOfStudyAndSectionOrderByVersionNoDesc(dept.getId(), request.yearOfStudy(), request.section().trim())
                .stream()
                .map(Timetable::getVersionNo)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(0) + 1;

        Timetable tt = timetableRepository.save(Timetable.builder()
                .department(dept)
                .yearOfStudy(request.yearOfStudy())
                .semester(request.semester())
                .section(request.section().trim().toUpperCase())
                .versionNo(nextVersion)
                .isActive(false)
                .createdBy(createdBy)
                .build());

        List<TimetableEntryRequest> entries = request.entries() == null ? List.of() : request.entries();
        validateEntries(entries);
        validateContinuousHoursSameFaculty(entries);

        List<TimetableEntry> toSave = new ArrayList<>();
        for (TimetableEntryRequest e : entries) {
            Course course = courseRepository.findById(e.courseId())
                    .orElseThrow(() -> new NotFoundException("Course not found: " + e.courseId()));
            Venue venue = venueRepository.findById(e.venueId())
                    .orElseThrow(() -> new NotFoundException("Venue not found: " + e.venueId()));

            Faculty defaultFaculty = null;
            if (e.defaultFacultyId() != null) {
                defaultFaculty = facultyRepository.findById(e.defaultFacultyId())
                        .orElseThrow(() -> new NotFoundException("Faculty not found: " + e.defaultFacultyId()));
            }

            if (!isMonToSat(e.dayOfWeek())) {
                throw new BadRequestException("Timetable supports only Monday-Saturday");
            }

            TimetableEntry te = TimetableEntry.builder()
                    .timetable(tt)
                    .departmentId(dept.getId())
                    .yearOfStudy(tt.getYearOfStudy())
                    .semester(tt.getSemester())
                    .section(tt.getSection())
                    .isActive(false)
                    .dayOfWeek(e.dayOfWeek())
                    .hourNumber(e.hourNumber())
                    .course(course)
                    .venue(venue)
                    .type(e.type())
                    .defaultFaculty(defaultFaculty)
                    .build();
            toSave.add(te);
        }

        timetableEntryRepository.saveAll(toSave);
        return toResponse(tt, toSave);
    }

    @Override
    @Transactional
    public TimetableResponse activate(Long timetableId) {
        Timetable tt = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new NotFoundException("Timetable not found: " + timetableId));

        // deactivate existing active timetable for same (dept/year/section)
        timetableRepository.findByDepartmentIdAndYearOfStudyAndSectionAndIsActiveTrue(
                        tt.getDepartment().getId(), tt.getYearOfStudy(), tt.getSection())
                .ifPresent(active -> {
                    active.setIsActive(false);
                    timetableRepository.save(active);
                    List<TimetableEntry> oldEntries = timetableEntryRepository.findByTimetableId(active.getId());
                    for (TimetableEntry oe : oldEntries) {
                        oe.setActive(false);
                    }
                    timetableEntryRepository.saveAll(oldEntries);
                });

        tt.setIsActive(true);
        timetableRepository.save(tt);

        List<TimetableEntry> entries = timetableEntryRepository.findByTimetableId(tt.getId());
        for (TimetableEntry te : entries) {
            te.setDepartmentId(tt.getDepartment().getId());
            te.setYearOfStudy(tt.getYearOfStudy());
            te.setSemester(tt.getSemester());
            te.setSection(tt.getSection());
            te.setActive(true);
        }

        try {
            timetableEntryRepository.saveAll(entries);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Timetable conflicts detected (venue/section already booked in active timetable)");
        }

        return toResponse(tt, entries);
    }

    @Override
    public TimetableResponse getActive(Long departmentId, Integer yearOfStudy, String section) {
        Timetable tt = timetableRepository.findByDepartmentIdAndYearOfStudyAndSectionAndIsActiveTrue(departmentId, yearOfStudy, section.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Active timetable not found"));
        List<TimetableEntry> entries = timetableEntryRepository.findByTimetableId(tt.getId());
        return toResponse(tt, entries);
    }

    private void validateEntries(List<TimetableEntryRequest> entries) {
        Set<String> seen = new HashSet<>();
        for (TimetableEntryRequest e : entries) {
            String key = e.dayOfWeek() + ":" + e.hourNumber();
            if (!seen.add(key)) {
                throw new BadRequestException("Duplicate slot in timetable: " + key);
            }
        }
    }

    private void validateContinuousHoursSameFaculty(List<TimetableEntryRequest> entries) {
        Map<DayOfWeek, Map<Integer, TimetableEntryRequest>> map = new HashMap<>();
        for (TimetableEntryRequest e : entries) {
            map.computeIfAbsent(e.dayOfWeek(), d -> new HashMap<>()).put(e.hourNumber(), e);
        }
        for (var day : map.entrySet()) {
            for (int hour = 1; hour <= 6; hour++) {
                TimetableEntryRequest a = day.getValue().get(hour);
                TimetableEntryRequest b = day.getValue().get(hour + 1);
                if (a == null || b == null) continue;
                if (Objects.equals(a.courseId(), b.courseId())) {
                    if (a.defaultFacultyId() == null || b.defaultFacultyId() == null || !Objects.equals(a.defaultFacultyId(), b.defaultFacultyId())) {
                        throw new BadRequestException("Continuous hours for same course require same faculty assignment (" + day.getKey() + " hour " + hour + "-" + (hour + 1) + ")");
                    }
                }
            }
        }
    }

    private static boolean isMonToSat(DayOfWeek d) {
        return d != null && d != DayOfWeek.SUNDAY;
    }

    private TimetableResponse toResponse(Timetable tt, List<TimetableEntry> entries) {
        List<TimetableEntryResponse> er = entries.stream()
                .sorted(Comparator.comparing(TimetableEntry::getDayOfWeek).thenComparing(TimetableEntry::getHourNumber))
                .map(e -> new TimetableEntryResponse(
                        e.getId(),
                        e.getDayOfWeek(),
                        e.getHourNumber(),
                        e.getCourse().getId(),
                        e.getVenue().getId(),
                        e.getType(),
                        e.getDefaultFaculty() != null ? e.getDefaultFaculty().getId() : null
                )).toList();
        return new TimetableResponse(
                tt.getId(),
                tt.getDepartment().getId(),
                tt.getYearOfStudy(),
                tt.getSemester(),
                tt.getSection(),
                tt.getVersionNo(),
                Boolean.TRUE.equals(tt.getIsActive()),
                er
        );
    }
}

