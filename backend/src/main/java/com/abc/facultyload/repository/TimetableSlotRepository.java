package com.abc.facultyload.repository;

import com.abc.facultyload.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findAllByTimetableId(Long timetableId);
    void deleteAllByTimetableId(Long timetableId);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM TimetableSlot s WHERE s.timetable.section.department.id = :deptId AND s.timetable.status = 'ACTIVE' AND s.venue IS NOT NULL")
    List<TimetableSlot> findAllActiveSlotsWithVenueByDept(Long deptId);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE TimetableSlot t SET t.defaultFaculty = null WHERE t.defaultFaculty.id = :facultyId")
    void nullifyDefaultFaculty(Long facultyId);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE TimetableSlot t SET t.additionalFaculty = null WHERE t.additionalFaculty.id = :facultyId")
    void nullifyAdditionalFaculty(Long facultyId);
}
