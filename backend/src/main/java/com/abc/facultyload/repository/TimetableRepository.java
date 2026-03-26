package com.abc.facultyload.repository;

import com.abc.facultyload.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findAllBySectionId(Long sectionId);
    List<Timetable> findAllByStatusAndSection_Department_Id(Timetable.TimetableStatus status, Long deptId);

    @Query("SELECT t FROM Timetable t WHERE t.status = 'ACTIVE' AND t.section.department.id = :deptId")
    List<Timetable> findActiveTimetablesByDeptId(Long deptId);
}
