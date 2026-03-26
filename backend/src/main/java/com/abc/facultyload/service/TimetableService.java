package com.abc.facultyload.service;

import com.abc.facultyload.dto.request.TimetableSlotRequest;
import com.abc.facultyload.entity.*;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final TimetableSlotRepository slotRepository;
    private final SectionRepository sectionRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final VenueRepository venueRepository;
    private final FacultyRepository facultyRepository;
    private final WorkAssignmentRepository workAssignmentRepository;

    public List<Timetable> getTimetablesByDept(Long deptId) {
        return timetableRepository.findAllByStatusAndSection_Department_Id(Timetable.TimetableStatus.ACTIVE, deptId);
    }

    public List<Timetable> getTimetablesBySection(Long sectionId) {
        return timetableRepository.findAllBySectionId(sectionId);
    }

    public List<TimetableSlot> getSlotsByTimetable(Long timetableId) {
        return slotRepository.findAllByTimetableId(timetableId);
    }

    @Transactional
    public Section createSection(Long deptId, Integer year, Integer semester, String sectionName) {
        if (sectionRepository.existsByDepartmentIdAndYearAndSemesterAndSectionName(deptId, year, semester, sectionName))
            throw new AppException("Section already exists", HttpStatus.BAD_REQUEST);

        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));

        Section section = Section.builder()
                .department(dept)
                .year(year)
                .semester(semester)
                .sectionName(sectionName)
                .build();
        section = sectionRepository.save(section);

        // Auto-create initial Timetable for this Section
        Timetable timetable = Timetable.builder()
                .section(section)
                .status(Timetable.TimetableStatus.INACTIVE)
                .timetableLabel("Timetable 1")
                .build();
        timetableRepository.save(timetable);

        return section;
    }



    @Transactional
    public void saveTimetableSlots(Long timetableId, List<TimetableSlotRequest> slots) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new AppException("Timetable not found", HttpStatus.NOT_FOUND));

        List<TimetableSlot> existingSlots = slotRepository.findAllByTimetableId(timetableId);
        Map<String, TimetableSlot> existingMap = new HashMap<>();
        for (TimetableSlot s : existingSlots) {
            existingMap.put(s.getDayOfWeek() + "_" + s.getHour(), s);
        }

        Set<String> processedKeys = new HashSet<>();
        List<TimetableSlot> toSave = new ArrayList<>();

        for (TimetableSlotRequest s : slots) {
            String key = s.getDayOfWeek() + "_" + s.getHour();
            processedKeys.add(key);

            Course course = s.getCourseId() != null ? courseRepository.findById(s.getCourseId()).orElse(null) : null;
            Venue venue = s.getVenueId() != null ? venueRepository.findById(s.getVenueId()).orElse(null) : null;
            Faculty faculty = s.getDefaultFacultyId() != null ? facultyRepository.findById(s.getDefaultFacultyId()).orElse(null) : null;
            Faculty additionalFaculty = s.getAdditionalFacultyId() != null ? facultyRepository.findById(s.getAdditionalFacultyId()).orElse(null) : null;
            TimetableSlot.SlotType type = s.getSlotType() != null ? TimetableSlot.SlotType.valueOf(s.getSlotType()) : TimetableSlot.SlotType.THEORY;

            // Clear additional faculty if not a lab
            if (type != TimetableSlot.SlotType.LAB) {
                additionalFaculty = null;
            }

            TimetableSlot existing = existingMap.get(key);
            if (existing != null) {
                existing.setCourse(course);
                existing.setVenue(venue);
                existing.setDefaultFaculty(faculty);
                existing.setAdditionalFaculty(additionalFaculty);
                existing.setSlotType(type);
                toSave.add(existing);
            } else {
                toSave.add(TimetableSlot.builder()
                        .timetable(timetable)
                        .dayOfWeek(s.getDayOfWeek())
                        .hour(s.getHour())
                        .course(course)
                        .venue(venue)
                        .defaultFaculty(faculty)
                        .additionalFaculty(additionalFaculty)
                        .slotType(type)
                        .build());
            }
        }

        List<TimetableSlot> toDelete = new ArrayList<>();
        for (TimetableSlot s : existingSlots) {
            if (!processedKeys.contains(s.getDayOfWeek() + "_" + s.getHour())) {
                toDelete.add(s);
            }
        }

        if (!toDelete.isEmpty()) {
            workAssignmentRepository.nullifyTimetableSlotIn(toDelete);
            slotRepository.deleteAll(toDelete);
        }

        slotRepository.saveAll(toSave);
    }

    @Transactional
    public Timetable setTimetableStatus(Long timetableId, Timetable.TimetableStatus status) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new AppException("Timetable not found", HttpStatus.NOT_FOUND));
        timetable.setStatus(status);
        return timetableRepository.save(timetable);
    }

    @Transactional
    public Timetable copyToNewSection(Long originalTimetableId, Long deptId, Integer year, Integer semester, String sectionName) {
        // Create new Section (which auto-creates 'Timetable 1')
        Section newSec = createSection(deptId, year, semester, sectionName);
        
        // Find the auto-created timetable
        Timetable newTimetable = timetableRepository.findAllBySectionId(newSec.getId()).get(0);
        
        // Copy slots from original
        List<TimetableSlot> originalSlots = slotRepository.findAllByTimetableId(originalTimetableId);
        List<TimetableSlot> copiedSlots = new ArrayList<>();
        
        for (TimetableSlot s : originalSlots) {
            copiedSlots.add(TimetableSlot.builder()
                    .timetable(newTimetable)
                    .dayOfWeek(s.getDayOfWeek())
                    .hour(s.getHour())
                    .course(s.getCourse())
                    .venue(s.getVenue())
                    .defaultFaculty(s.getDefaultFaculty())
                    .additionalFaculty(s.getAdditionalFaculty())
                    .slotType(s.getSlotType())
                    .build());
        }
        
        slotRepository.saveAll(copiedSlots);
        return newTimetable;
    }

    @Transactional
    public void deleteTimetable(Long timetableId) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new AppException("Timetable not found", HttpStatus.NOT_FOUND));
        
        Long sectionId = timetable.getSection().getId();
        
        // Step 1: Fetch slots BEFORE deleting them
        List<TimetableSlot> slots = slotRepository.findAllByTimetableId(timetableId);
        
        // Step 2: Nullify work assignment references (so historical records survive)
        if (!slots.isEmpty()) {
            workAssignmentRepository.nullifyTimetableSlotIn(slots);
        }
        
        // Step 3: Now it's safe to delete the slots
        slotRepository.deleteAllByTimetableId(timetableId);
        
        // Step 4: Delete the timetable
        timetableRepository.deleteById(timetableId);
        
        // Step 5: Cascade-delete the section (1:1 relationship)
        sectionRepository.deleteById(sectionId);
    }

    public List<Section> getSectionsByDept(Long deptId) {
        return sectionRepository.findAllByDepartmentId(deptId);
    }

    public Map<String, List<Long>> getOccupiedVenuesMatrix(Long deptId, Long currentTimetableId) {
        // [day_hour] -> list of venueIds
        Map<String, List<Long>> matrix = new HashMap<>();
        List<TimetableSlot> activeSlots = slotRepository.findAllActiveSlotsWithVenueByDept(deptId);
        
        for (TimetableSlot slot : activeSlots) {
            // Exclude the current timetable we're editing so we don't block ourselves
            if (currentTimetableId != null && slot.getTimetable().getId().equals(currentTimetableId)) {
                continue;
            }
            if (slot.getVenue() == null || slot.getDayOfWeek() == null || slot.getHour() == null) continue;
            
            String key = slot.getDayOfWeek() + "_" + slot.getHour();
            matrix.computeIfAbsent(key, k -> new ArrayList<>()).add(slot.getVenue().getId());
        }
        return matrix;
    }
    public Map<String, List<Long>> getOccupiedFacultyMatrix(Long deptId, Long currentTimetableId) {
        // [day_hour] -> list of facultyIds already assigned in OTHER timetables
        Map<String, List<Long>> matrix = new HashMap<>();
        List<TimetableSlot> activeSlots = slotRepository.findAllActiveSlotsWithVenueByDept(deptId);

        for (TimetableSlot slot : activeSlots) {
            if (currentTimetableId != null && slot.getTimetable().getId().equals(currentTimetableId)) continue;
            if (slot.getDayOfWeek() == null || slot.getHour() == null) continue;
            String key = slot.getDayOfWeek() + "_" + slot.getHour();
            
            if (slot.getDefaultFaculty() != null) {
                matrix.computeIfAbsent(key, k -> new ArrayList<>()).add(slot.getDefaultFaculty().getId());
            }
            if (slot.getAdditionalFaculty() != null && slot.getSlotType() == TimetableSlot.SlotType.LAB) {
                matrix.computeIfAbsent(key, k -> new ArrayList<>()).add(slot.getAdditionalFaculty().getId());
            }
        }
        return matrix;
    }
}
