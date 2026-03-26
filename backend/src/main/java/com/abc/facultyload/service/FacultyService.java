package com.abc.facultyload.service;

import com.abc.facultyload.dto.request.FacultyRequest;
import com.abc.facultyload.dto.response.FacultyResponse;
import com.abc.facultyload.entity.*;
import com.abc.facultyload.exception.AppException;
import com.abc.facultyload.repository.*;
import com.abc.facultyload.util.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyMonthlyLoadRepository monthlyLoadRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExcelExporter excelExporter;
    
    private final ArchivedFacultyRepository archivedFacultyRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final CourseFacultyMappingRepository courseFacultyMappingRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationRepository notificationRepository;

    public List<FacultyResponse> getAllFaculty() {
        return facultyRepository.findAllByActiveTrue().stream().map(this::toResponse).toList();
    }

    public List<FacultyResponse> getFacultyByDepartment(Long deptId) {
        return facultyRepository.findAllByDepartmentIdAndActiveTrue(deptId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public FacultyResponse createFaculty(FacultyRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new AppException("Email already registered", HttpStatus.BAD_REQUEST);
        if (userRepository.existsByRollNumber(req.getRollNumber()))
            throw new AppException("Roll number already registered", HttpStatus.BAD_REQUEST);

        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));

        Role.RoleName roleName = "HOD".equalsIgnoreCase(req.getRole()) ? Role.RoleName.HOD : Role.RoleName.FACULTY;

        // If new role is HOD, demote existing HOD in dept
        if (roleName == Role.RoleName.HOD) {
            Optional<Faculty> existingHod = facultyRepository.findHodByDeptId(dept.getId(), Role.RoleName.HOD);
            existingHod.ifPresent(hod -> {
                Role facultyRole = roleRepository.findByName(Role.RoleName.FACULTY)
                        .orElseThrow(() -> new AppException("Faculty role not found", HttpStatus.INTERNAL_SERVER_ERROR));
                hod.getUser().setRole(facultyRole);
                userRepository.save(hod.getUser());
            });
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException("Role not found", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
                .email(req.getEmail())
                .rollNumber(req.getRollNumber())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .name(req.getName())
                .active(true)
                .build();
        userRepository.save(user);

        Faculty faculty = Faculty.builder()
                .user(user)
                .department(dept)
                .name(req.getName())
                .rollNumber(req.getRollNumber())
                .email(req.getEmail())
                .phone(req.getPhone())
                .bloodGroup(req.getBloodGroup())
                .active(true)
                .build();
        faculty = facultyRepository.save(faculty);
        return toResponse(faculty);
    }

    @Transactional
    public FacultyResponse updateFaculty(Long id, FacultyRequest req) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new AppException("Faculty not found", HttpStatus.NOT_FOUND));

        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));

        Role.RoleName roleName = "HOD".equalsIgnoreCase(req.getRole()) ? Role.RoleName.HOD : Role.RoleName.FACULTY;

        // Handle HOD promotion
        if (roleName == Role.RoleName.HOD && !faculty.getUser().getRole().getName().equals(Role.RoleName.HOD)) {
            Optional<Faculty> existingHod = facultyRepository.findHodByDeptId(dept.getId(), Role.RoleName.HOD);
            existingHod.ifPresent(hod -> {
                Role facultyRole = roleRepository.findByName(Role.RoleName.FACULTY).orElseThrow();
                hod.getUser().setRole(facultyRole);
                userRepository.save(hod.getUser());
            });
        }

        Role role = roleRepository.findByName(roleName).orElseThrow();
        faculty.getUser().setRole(role);
        faculty.getUser().setName(req.getName());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            faculty.getUser().setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(faculty.getUser());

        faculty.setDepartment(dept);
        faculty.setName(req.getName());
        faculty.setPhone(req.getPhone());
        faculty.setBloodGroup(req.getBloodGroup());
        faculty = facultyRepository.save(faculty);
        return toResponse(faculty);
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new AppException("Faculty not found", HttpStatus.NOT_FOUND));
                
        // 1. Backup to ArchivedFaculty
        ArchivedFaculty archive = ArchivedFaculty.builder()
                .originalId(faculty.getId())
                .name(faculty.getName())
                .email(faculty.getEmail())
                .rollNumber(faculty.getRollNumber())
                .departmentName(faculty.getDepartment() != null ? faculty.getDepartment().getDeptName() : null)
                .roleName(faculty.getUser().getRole().getName().name())
                .phone(faculty.getPhone())
                .deletedAt(java.time.LocalDateTime.now())
                .build();
        archivedFacultyRepository.save(archive);

        // 2. Cascade Wipe Relations (prevents foreign key constraint errors)
        timetableSlotRepository.nullifyDefaultFaculty(id);
        timetableSlotRepository.nullifyAdditionalFaculty(id);
        workAssignmentRepository.deleteAllByFacultyId(id);
        courseFacultyMappingRepository.deleteAllByFacultyId(id);
        leaveRequestRepository.nullifyTempHod(id);
        leaveRequestRepository.deleteAllByFacultyId(id);
        monthlyLoadRepository.deleteAllByFacultyId(id);
        notificationRepository.deleteAllByUserId(faculty.getUser().getId());

        // 3. Delete original entity safely
        User user = faculty.getUser();
        facultyRepository.delete(faculty);
        userRepository.delete(user);
    }

    public ByteArrayInputStream exportFacultyExcel() {
        List<Faculty> faculties = facultyRepository.findAll();
        List<String> headers = List.of("Name", "Roll Number", "Email", "Phone", "Department", "Blood Group", "Role", "Status");
        List<Map<String, Object>> data = faculties.stream().map(f -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Name", f.getName());
            row.put("Roll Number", f.getRollNumber());
            row.put("Email", f.getEmail());
            row.put("Phone", f.getPhone());
            row.put("Department", f.getDepartment().getDeptName());
            row.put("Blood Group", f.getBloodGroup());
            row.put("Role", f.getUser().getRole().getName().name());
            row.put("Status", f.isActive() ? "Active" : "Inactive");
            return row;
        }).toList();
        return excelExporter.exportToExcel("Faculty List", headers, data);
    }

    public FacultyResponse toResponse(Faculty f) {
        LocalDate now = LocalDate.now();
        Integer monthHours = monthlyLoadRepository
                .findByFacultyIdAndYearAndMonth(f.getId(), now.getYear(), now.getMonthValue())
                .map(FacultyMonthlyLoad::getTotalHours).orElse(0);

        return FacultyResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .rollNumber(f.getRollNumber())
                .email(f.getEmail())
                .phone(f.getPhone())
                .bloodGroup(f.getBloodGroup())
                .departmentName(f.getDepartment().getDeptName())
                .departmentId(f.getDepartment().getId())
                .role(f.getUser().getRole().getName().name())
                .isActive(f.isActive())
                .currentMonthHours(monthHours)
                .build();
    }
}
