package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
