package com.abc.facultyload.repository;

import com.abc.facultyload.entity.ArchivedFaculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedFacultyRepository extends JpaRepository<ArchivedFaculty, Long> {
}
