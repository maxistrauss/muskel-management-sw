package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.Course;
import de.oth.muskelmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTrainer(User trainer);
    List<Course> findByRoom(de.oth.muskelmanagement.model.Room room);
}

