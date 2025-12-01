package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.Enrollment;
import de.oth.muskelmanagement.model.Course;
import de.oth.muskelmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourse(Course course);
    Optional<Enrollment> findByCourseAndUser(Course course, User user);
}
