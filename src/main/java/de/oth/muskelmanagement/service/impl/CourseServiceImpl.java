package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.CourseDto;
import de.oth.muskelmanagement.dto.ExerciseDto;
import de.oth.muskelmanagement.dto.RoomDto;
import de.oth.muskelmanagement.dto.UserDto;
import de.oth.muskelmanagement.model.entity.*;
import de.oth.muskelmanagement.model.enums.AttendanceStatus;
import de.oth.muskelmanagement.repository.*;
import de.oth.muskelmanagement.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final RoomRepository roomRepository;

    public CourseServiceImpl(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,
                             UserRepository userRepository, ExerciseRepository exerciseRepository, RoomRepository roomRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.roomRepository = roomRepository;
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

    @Override
    public Page<Course> findEnrolledCoursesByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        List<Course> enrolledCourses = enrollments.stream()
                .map(Enrollment::getCourse)
                .filter(Course::isActive) // Nur aktive Kurse anzeigen
                .distinct()
                .sorted((c1, c2) -> c2.getId().compareTo(c1.getId())) // Neueste zuerst
                .collect(java.util.stream.Collectors.toList());
        
        // Pagination manuell implementieren
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), enrolledCourses.size());
        if (start > enrolledCourses.size()) {
            start = enrolledCourses.size();
            end = start;
        }
        List<Course> pagedCourses = enrolledCourses.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(pagedCourses, pageable, enrolledCourses.size());
    }

    @Override
    @Transactional
    public Course addExerciseToCourse(Long courseId, Long exerciseId) {
        Course course = findById(courseId);
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

        course.addExercise(exercise);
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course removeExerciseFromCourse(Long courseId, Long exerciseId) {
        Course course = findById(courseId);
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));

        course.removeExercise(exercise);
        return courseRepository.save(course);
    }

    @Override
    public Set<Exercise> getExercisesByCourse(Long courseId) {
        Course course = findById(courseId);
        return course.getExercises();
    }

    @Override
    @Transactional
    public Course bulkAssignExercises(Long courseId, List<Long> exerciseIds) {
        Course course = findById(courseId);

        // Clear existing exercise assignments
        course.getExercises().clear();

        // Add new exercise assignments
        if (exerciseIds != null && !exerciseIds.isEmpty()) {
            for (Long exerciseId : exerciseIds) {
                Exercise exercise = exerciseRepository.findById(exerciseId)
                        .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + exerciseId));
                course.addExercise(exercise);
            }
        }

        return courseRepository.save(course);
    }

    // New DTO methods
    @Override
    public CourseDto getCourseDtoById(Long id) {
        Course course = findById(id);
        return toCourseDto(course);
    }

    @Override
    @Transactional
    public Course saveCourseFromDto(CourseDto courseDto) {
        Course course = new Course();
        updateCourseFromDto(course, courseDto);
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourseFromDto(Long id, CourseDto courseDto) {
        Course existingCourse = findById(id);
        updateCourseFromDto(existingCourse, courseDto);
        return courseRepository.save(existingCourse);
    }

    // Helper methods for conversion
    private void updateCourseFromDto(Course course, CourseDto courseDto) {
        course.setName(courseDto.getName());
        course.setDescription(courseDto.getDescription());
        course.setCapacity(courseDto.getCapacity());
        course.setActive(courseDto.isActive());
        course.setStartDate(courseDto.getStartDate());
        course.setEndDate(courseDto.getEndDate());
        course.setStartTime(courseDto.getStartTime()); // Added
        course.setDurationMinutes(courseDto.getDurationMinutes()); // Added
        course.setDaysOfWeek(courseDto.getDaysOfWeek());

        if (courseDto.getTrainer() != null && courseDto.getTrainer().getId() != null) {
            User trainer = userRepository.findById(courseDto.getTrainer().getId())
                    .orElseThrow(() -> new RuntimeException("Trainer not found with id: " + courseDto.getTrainer().getId()));
            course.setTrainer(trainer);
        } else {
            course.setTrainer(null); // Explicitly set to null if not provided
        }

        if (courseDto.getRoom() != null && courseDto.getRoom().getId() != null) {
            Room room = roomRepository.findById(courseDto.getRoom().getId())
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + courseDto.getRoom().getId()));
            course.setRoom(room);
        } else {
            course.setRoom(null);
        }
    }

    private CourseDto toCourseDto(Course course) {
        return new CourseDto(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getCapacity(),
                course.isActive(),
                toUserDto(course.getTrainer()),
                toRoomDto(course.getRoom()),
                course.getStartDate(),
                course.getEndDate(),
                course.getStartTime(),
                course.getDurationMinutes(),
                course.getDaysOfWeek(),
                toExerciseDto(course.getExercises())
        );
    }

    private UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return userDto;
    }

    private RoomDto toRoomDto(Room room) {
        if (room == null) {
            return null;
        }
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setName(room.getName());
        return roomDto;
    }

    private Set<ExerciseDto> toExerciseDto(Set<Exercise> exercises) {
        if (exercises == null) {
            return null;
        }
        return exercises.stream()
                .map(this::toSingleExerciseDto)
                .collect(Collectors.toSet());
    }

    private ExerciseDto toSingleExerciseDto(Exercise exercise) {
        if (exercise == null) {
            return null;
        }
        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setId(exercise.getId());
        exerciseDto.setName(exercise.getName());
        exerciseDto.setExternalId(exercise.getExternalId());
        exerciseDto.setGifUrl(exercise.getGifUrl());
        exerciseDto.setBodyPart(exercise.getBodyPart());
        exerciseDto.setEquipment(exercise.getEquipment());
        exerciseDto.setTarget(exercise.getTarget());
        exerciseDto.setSecondaryMuscles(exercise.getSecondaryMuscles());
        exerciseDto.setInstructions(exercise.getInstructions());
        exerciseDto.setCreatedAt(exercise.getCreatedAt());
        exerciseDto.setLastSyncedAt(exercise.getLastSyncedAt());
        return exerciseDto;
    }
}
