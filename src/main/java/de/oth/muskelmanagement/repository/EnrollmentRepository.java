package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourse(Course course);
    List<Enrollment> findByUser(User user);
    Optional<Enrollment> findByCourseAndUser(Course course, User user);
}
