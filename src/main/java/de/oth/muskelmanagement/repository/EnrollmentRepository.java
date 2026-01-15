package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourse(Course course);
    org.springframework.data.domain.Page<Enrollment> findByCourse(Course course, org.springframework.data.domain.Pageable pageable);
    List<Enrollment> findByUser(User user);
    Optional<Enrollment> findByCourseAndUser(Course course, User user);

    long countByCourseAndStatus(Course course, EnrollmentStatus status);

    List<Enrollment> findByCourseAndStatusOrderByCreatedAtAsc(Course course, EnrollmentStatus status);
    org.springframework.data.domain.Page<Enrollment> findByCourseAndStatus(Course course, EnrollmentStatus status, org.springframework.data.domain.Pageable pageable);

    List<Enrollment> findByUserAndStatus(User user, EnrollmentStatus status);

    org.springframework.data.domain.Page<Enrollment> findByUser(User user,
            org.springframework.data.domain.Pageable pageable);
}
