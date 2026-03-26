package com.abc.facultyload.repository;

import com.abc.facultyload.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRollNumber(String rollNumber);

    @Query("SELECT u FROM User u WHERE u.email = :login OR u.rollNumber = :login")
    Optional<User> findByEmailOrRollNumber(String login);

    boolean existsByEmail(String email);
    boolean existsByRollNumber(String rollNumber);
}
