package com.abc.facultyload.service;

import com.abc.facultyload.entity.CourseFacultyMapping;
import com.abc.facultyload.entity.Course;
import com.abc.facultyload.entity.Faculty;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HodService {

    private final FacultyRepository facultyRepository;
    private final FacultyMonthlyLoadRepository monthlyLoadRepository;
    private final CourseFacultyMappingRepository mappingRepository;
    private final CourseRepository courseRepository;

    // HOD Dashboard: dept faculty list with monthly hours
    public Map<String, Object> getDeptDashboard(Long deptId) {
        LocalDate now = LocalDate.now();
        List<Faculty> faculties = facultyRepository.findAllByDepartmentIdAndActiveTrue(deptId);

        List<Map<String, Object>> facultyData = faculties.stream().map(f -> {
            int hours = monthlyLoadRepository.findByFacultyIdAndYearAndMonth(f.getId(), now.getYear(), now.getMonthValue())
                    .map(l -> l.getTotalHours()).orElse(0);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("name", f.getName());
            m.put("rollNumber", f.getRollNumber());
            m.put("monthlyHours", hours);
            m.put("role", f.getUser().getRole().getName().name());
            return m;
        }).toList();

        double avg = facultyData.stream()
                .mapToInt(m -> (int) m.get("monthlyHours")).average().orElse(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("faculty", facultyData);
        result.put("averageHours", avg);
        result.put("year", now.getYear());
        result.put("month", now.getMonthValue());
        return result;
    }

    // HOD Performance Analysis
    public Map<String, Object> getPerformanceStats(Long deptId) {
        LocalDate now = LocalDate.now();
        List<Faculty> faculties = facultyRepository.findAllByDepartmentIdAndActiveTrue(deptId);
        int total = faculties.size();

        List<Integer> hours = faculties.stream().map(f ->
                monthlyLoadRepository.findByFacultyIdAndYearAndMonth(f.getId(), now.getYear(), now.getMonthValue())
                        .map(l -> l.getTotalHours()).orElse(0)
        ).toList();

        double avg = hours.stream().mapToInt(Integer::intValue).average().orElse(0);
        double range = avg + (avg * 0.18);

        long belowRange = hours.stream().filter(h -> h < range).count();
        double performance = total > 0 ? (double) belowRange / total * 100 : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalFaculty", total);
        result.put("averageHours", avg);
        result.put("rangeThreshold", range);
        result.put("belowRangeCount", belowRange);
        result.put("performancePercent", performance);

        List<Map<String, Object>> chart = new ArrayList<>();
        for (int i = 0; i < faculties.size(); i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", faculties.get(i).getName());
            item.put("hours", hours.get(i));
            chart.add(item);
        }
        result.put("chartData", chart);
        return result;
    }

    // Course-Faculty Mapping CRUD
    @Transactional
    public CourseFacultyMapping addMapping(Long facultyId, Long courseId, String type) {
        if (mappingRepository.existsByFacultyIdAndCourseId(facultyId, courseId))
            throw new AppException("Mapping already exists", HttpStatus.BAD_REQUEST);

        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new AppException("Faculty not found", HttpStatus.NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Course not found", HttpStatus.NOT_FOUND));

        CourseFacultyMapping mapping = CourseFacultyMapping.builder()
                .faculty(faculty)
                .course(course)
                .type(CourseFacultyMapping.MappingType.valueOf(type))
                .build();
        return mappingRepository.save(mapping);
    }

    @Transactional
    public void removeMapping(Long mappingId) {
        mappingRepository.deleteById(mappingId);
    }

    public List<CourseFacultyMapping> getMappingsByDept(Long deptId) {
        List<Faculty> faculties = facultyRepository.findAllByDepartmentId(deptId);
        List<CourseFacultyMapping> all = new ArrayList<>();
        for (Faculty f : faculties) {
            all.addAll(mappingRepository.findAllByFacultyId(f.getId()));
        }
        return all;
    }
}
