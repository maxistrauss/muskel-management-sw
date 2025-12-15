package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.CourseDto;
import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.model.entity.Exercise;
import de.oth.muskelmanagement.model.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface CourseService {
    Page<Course> findAll(Pageable pageable);
    Page<Course> findByTrainerId(Long trainerId, Pageable pageable);
    Course findById(Long id);
    Course save(Course course);
    void deleteById(Long id);

    Enrollment addMember(Long courseId, Long userId);
    void removeMember(Long courseId, Long userId);
    Enrollment setAttendance(Long courseId, Long userId, AttendanceStatus status);
    List<Enrollment> listEnrollments(Long courseId);
    
    // Enrollment management
    Page<Course> findEnrolledCoursesByUser(Long userId, Pageable pageable);
    
    // Exercise Management
    Course addExerciseToCourse(Long courseId, Long exerciseId);
    Course removeExerciseFromCourse(Long courseId, Long exerciseId);
    Set<Exercise> getExercisesByCourse(Long courseId);
    Course bulkAssignExercises(Long courseId, List<Long> exerciseIds);

    CourseDto getCourseDtoById(Long id);
    Course saveCourseFromDto(CourseDto courseDto);
    Course updateCourseFromDto(Long id, CourseDto courseDto);
}
