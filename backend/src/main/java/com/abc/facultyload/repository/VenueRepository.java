package com.abc.facultyload.repository;

import com.abc.facultyload.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long>, JpaSpecificationExecutor<Venue> {
    List<Venue> findAllByDepartmentId(Long deptId);
    List<Venue> findAllByDepartmentIdAndActiveTrue(Long deptId);
    List<Venue> findAllByActiveTrue();
    boolean existsByVenueName(String venueName);

    @Query("SELECT v FROM Venue v WHERE v.department.id = :deptId AND v.active = true " +
           "AND v.id NOT IN (SELECT wa.venue.id FROM WorkAssignment wa WHERE wa.assignDate = :date AND wa.hour = :hour AND wa.venue IS NOT NULL)")
    List<Venue> findAvailableVenuesByDeptIdDateAndHour(Long deptId, LocalDate date, Integer hour);

    long countByDepartmentIdAndVenueType(Long deptId, Venue.VenueType venueType);
}
