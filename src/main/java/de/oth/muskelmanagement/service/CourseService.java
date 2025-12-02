package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Course;
import de.oth.muskelmanagement.model.Enrollment;
import de.oth.muskelmanagement.model.Exercise;
import de.oth.muskelmanagement.model.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface CourseService {
    Page<Course> findAll(Pageable pageable);
    Course findById(Long id);
    Course save(Course course);
    void deleteById(Long id);

    Enrollment addMember(Long courseId, Long userId);
    void removeMember(Long courseId, Long userId);
    Enrollment setAttendance(Long courseId, Long userId, AttendanceStatus status);
    List<Enrollment> listEnrollments(Long courseId);
    
    // Exercise Management
    Course addExerciseToCourse(Long courseId, Long exerciseId);
    Course removeExerciseFromCourse(Long courseId, Long exerciseId);
    Set<Exercise> getExercisesByCourse(Long courseId);
}
