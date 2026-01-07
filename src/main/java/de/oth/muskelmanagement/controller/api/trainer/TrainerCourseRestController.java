package de.oth.muskelmanagement.controller.api.trainer;

import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.model.enums.AttendanceStatus;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer/courses")
@PreAuthorize("hasRole('TRAINER')")
public class TrainerCourseRestController {

    private final CourseService courseService;
    private final UserService userService;

    public TrainerCourseRestController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<Course>> listMyCourses(@PageableDefault(size = 10) Pageable pageable,
                                                      Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Page<Course> page = courseService.findByTrainerId(currentUser.getId(), pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getMyCourse(@PathVariable Long id, Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course course = courseService.findById(id);
        ensureOwnership(course, currentUser.getId());
        return ResponseEntity.ok(course);
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course, Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        // Assign the current trainer as owner
        course.setTrainer(currentUser);
        Course saved = courseService.save(course);
        // Enroll creator as confirmed member
        courseService.enrollCreator(saved.getId(), currentUser.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course update,
                                               Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course existing = courseService.findById(id);
        ensureOwnership(existing, currentUser.getId());

        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setCapacity(update.getCapacity());
        existing.setActive(update.isActive());
        existing.setRoom(update.getRoom());
        existing.setStartDate(update.getStartDate());
        existing.setEndDate(update.getEndDate());
        existing.setStartTime(update.getStartTime());
        existing.setDurationMinutes(update.getDurationMinutes());
        existing.setDaysOfWeek(update.getDaysOfWeek());

        Course saved = courseService.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id, Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course existing = courseService.findById(id);
        ensureOwnership(existing, currentUser.getId());
        courseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Members management ---

    @GetMapping("/{id}/members")
    public ResponseEntity<List<Enrollment>> listMembers(@PathVariable Long id, Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course course = courseService.findById(id);
        ensureOwnership(course, currentUser.getId());
        return ResponseEntity.ok(courseService.listEnrollments(id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Enrollment> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body,
                                                Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course course = courseService.findById(id);
        ensureOwnership(course, currentUser.getId());
        Long userId = body.get("userId");
        Enrollment e = courseService.addMember(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(e);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId,
                                             Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course course = courseService.findById(id);
        ensureOwnership(course, currentUser.getId());
        courseService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/members/{userId}/attendance")
    public ResponseEntity<Enrollment> setAttendance(@PathVariable Long id, @PathVariable Long userId,
                                                    @RequestBody Map<String, String> body,
                                                    Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course course = courseService.findById(id);
        ensureOwnership(course, currentUser.getId());
        String status = body.get("status");
        AttendanceStatus s = AttendanceStatus.valueOf(status);
        Enrollment updated = courseService.setAttendance(id, userId, s);
        return ResponseEntity.ok(updated);
    }

    // --- Exercises management ---

    @PostMapping("/{id}/exercises")
    public ResponseEntity<Course> assignExercises(@PathVariable Long id,
                                                  @RequestBody(required = false) List<Long> exerciseIds,
                                                  Principal principal) {
        var currentUser = userService.findByEmail(principal.getName());
        Course course = courseService.findById(id);
        ensureOwnership(course, currentUser.getId());
        Course updated = courseService.bulkAssignExercises(id, exerciseIds != null ? exerciseIds : List.of());
        return ResponseEntity.ok(updated);
    }

    private void ensureOwnership(Course course, Long trainerId) {
        if (course.getTrainer() == null || !course.getTrainer().getId().equals(trainerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own courses");
        }
    }
}
