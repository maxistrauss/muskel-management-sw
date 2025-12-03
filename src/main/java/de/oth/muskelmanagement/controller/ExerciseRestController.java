package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.model.Course;
import de.oth.muskelmanagement.model.Exercise;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST API controller for exercise management
 */
@RestController
@RequestMapping("/api/exercises")
public class ExerciseRestController {

    private final ExerciseService exerciseService;
    private final CourseService courseService;

    public ExerciseRestController(ExerciseService exerciseService, CourseService courseService) {
        this.exerciseService = exerciseService;
        this.courseService = courseService;
    }

    @GetMapping
    public Page<Exercise> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String bodyPart,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) String target) {
        
        if (search != null && !search.isBlank()) {
            return exerciseService.searchExercises(search, PageRequest.of(page, size));
        } else if (bodyPart != null && !bodyPart.isBlank()) {
            return exerciseService.findByBodyPart(bodyPart, PageRequest.of(page, size));
        } else if (equipment != null && !equipment.isBlank()) {
            return exerciseService.findByEquipment(equipment, PageRequest.of(page, size));
        } else if (target != null && !target.isBlank()) {
            return exerciseService.findByTarget(target, PageRequest.of(page, size));
        } else {
            return exerciseService.findAll(PageRequest.of(page, size));
        }
    }

    @GetMapping("/{id}")
    public Exercise get(@PathVariable Long id) {
        return exerciseService.findById(id);
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, List<String>>> getFilters() {
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("bodyParts", exerciseService.findDistinctBodyParts());
        filters.put("equipment", exerciseService.findDistinctEquipment());
        filters.put("targets", exerciseService.findDistinctTargets());
        return ResponseEntity.ok(filters);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExercises", exerciseService.count());
        stats.put("bodyParts", exerciseService.findDistinctBodyParts().size());
        stats.put("equipmentTypes", exerciseService.findDistinctEquipment().size());
        stats.put("targetMuscles", exerciseService.findDistinctTargets().size());
        return ResponseEntity.ok(stats);
    }

    // Course-Exercise Management Endpoints
    @PostMapping("/courses/{courseId}/exercises/{exerciseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<Course> addExerciseToCourse(
            @PathVariable Long courseId,
            @PathVariable Long exerciseId) {
        Course updated = courseService.addExerciseToCourse(courseId, exerciseId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/courses/{courseId}/exercises/{exerciseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<Void> removeExerciseFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long exerciseId) {
        courseService.removeExerciseFromCourse(courseId, exerciseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/exercises")
    public ResponseEntity<Set<Exercise>> getCourseExercises(@PathVariable Long courseId) {
        Set<Exercise> exercises = courseService.getExercisesByCourse(courseId);
        return ResponseEntity.ok(exercises);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}