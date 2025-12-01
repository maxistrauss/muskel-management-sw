package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.*;
import de.oth.muskelmanagement.repository.CourseRepository;
import de.oth.muskelmanagement.repository.EnrollmentRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public CourseServiceImpl(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,
                             UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<Course> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public Course findById(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Override
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Enrollment addMember(Long courseId, Long userId) {
        Course course = findById(courseId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // check existing
        return enrollmentRepository.findByCourseAndUser(course, user).orElseGet(() -> {
            Enrollment e = new Enrollment(user, course);
            Enrollment saved = enrollmentRepository.save(e);
            course.addEnrollment(saved);
            courseRepository.save(course);
            return saved;
        });
    }

    @Override
    @Transactional
    public void removeMember(Long courseId, Long userId) {
        Course course = findById(courseId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        enrollmentRepository.findByCourseAndUser(course, user).ifPresent(e -> {
            course.removeEnrollment(e);
            enrollmentRepository.delete(e);
            courseRepository.save(course);
        });
    }

    @Override
    @Transactional
    public Enrollment setAttendance(Long courseId, Long userId, AttendanceStatus status) {
        Course course = findById(courseId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Enrollment e = enrollmentRepository.findByCourseAndUser(course, user)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        e.setAttendanceStatus(status);
        return enrollmentRepository.save(e);
    }

    @Override
    public List<Enrollment> listEnrollments(Long courseId) {
        Course course = findById(courseId);
        return enrollmentRepository.findByCourse(course);
    }
}
