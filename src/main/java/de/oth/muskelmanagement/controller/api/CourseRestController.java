package de.oth.muskelmanagement.controller.api;

import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.model.enums.AttendanceStatus;
import de.oth.muskelmanagement.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

    private final CourseService courseService;

    public CourseRestController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public Page<Course> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return courseService.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course course) {
        Course saved = courseService.save(course);
        return ResponseEntity.created(URI.create("/api/courses/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course) {
        Course existing = courseService.findById(id);
        existing.setName(course.getName());
        existing.setDescription(course.getDescription());
        existing.setCapacity(course.getCapacity());
        existing.setActive(course.isActive());
        return courseService.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public Enrollment addMember(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        return courseService.addMember(id, userId);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        courseService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/members/{userId}/attendance")
    public Enrollment setAttendance(@PathVariable Long id, @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        AttendanceStatus s = AttendanceStatus.valueOf(status);
        return courseService.setAttendance(id, userId, s);
    }

    @GetMapping("/{id}/members")
    public List<Enrollment> listMembers(@PathVariable Long id) {
        return courseService.listEnrollments(id);
    }
}
