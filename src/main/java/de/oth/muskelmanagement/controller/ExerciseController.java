package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.model.Exercise;
import de.oth.muskelmanagement.service.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for member exercise browsing
 */
@Controller
@RequestMapping("/member/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String bodyPart,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) String target,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        
        Page<Exercise> exercisePage;
        
        if (search != null && !search.isBlank()) {
            exercisePage = exerciseService.searchExercises(search, pageable);
            model.addAttribute("search", search);
        } else if (bodyPart != null && !bodyPart.isBlank()) {
            exercisePage = exerciseService.findByBodyPart(bodyPart, pageable);
            model.addAttribute("bodyPart", bodyPart);
        } else if (equipment != null && !equipment.isBlank()) {
            exercisePage = exerciseService.findByEquipment(equipment, pageable);
            model.addAttribute("equipment", equipment);
        } else if (target != null && !target.isBlank()) {
            exercisePage = exerciseService.findByTarget(target, pageable);
            model.addAttribute("target", target);
        } else {
            exercisePage = exerciseService.findAll(pageable);
        }
        
        model.addAttribute("exercisePage", exercisePage);
        model.addAttribute("exercises", exercisePage.getContent());
        
        // Add filter options - ensure they're never null
        model.addAttribute("bodyParts", exerciseService.findDistinctBodyParts() != null ?
                exerciseService.findDistinctBodyParts() : java.util.Collections.emptyList());
        model.addAttribute("equipmentTypes", exerciseService.findDistinctEquipment() != null ?
                exerciseService.findDistinctEquipment() : java.util.Collections.emptyList());
        model.addAttribute("targets", exerciseService.findDistinctTargets() != null ?
                exerciseService.findDistinctTargets() : java.util.Collections.emptyList());
        
        return "exercises";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseService.findById(id);
        model.addAttribute("exercise", exercise);
        return "exercise-details";
    }

    @GetMapping("/search")
    public String search(@RequestParam String q, @PageableDefault(size = 10) Pageable pageable, Model model) {
        Page<Exercise> exercisePage = exerciseService.searchExercises(q, pageable);
        model.addAttribute("exercisePage", exercisePage);
        model.addAttribute("exercises", exercisePage.getContent());
        model.addAttribute("search", q);
        model.addAttribute("bodyParts", exerciseService.findDistinctBodyParts());
        model.addAttribute("equipmentTypes", exerciseService.findDistinctEquipment());
        model.addAttribute("targets", exerciseService.findDistinctTargets());
        return "exercises";
    }

    @GetMapping("/bodyPart/{bodyPart}")
    public String byBodyPart(@PathVariable String bodyPart, @PageableDefault(size = 10) Pageable pageable, Model model) {
        Page<Exercise> exercisePage = exerciseService.findByBodyPart(bodyPart, pageable);
        model.addAttribute("exercisePage", exercisePage);
        model.addAttribute("exercises", exercisePage.getContent());
        model.addAttribute("bodyPart", bodyPart);
        model.addAttribute("bodyParts", exerciseService.findDistinctBodyParts());
        model.addAttribute("equipmentTypes", exerciseService.findDistinctEquipment());
        model.addAttribute("targets", exerciseService.findDistinctTargets());
        return "exercises";
    }

    @GetMapping("/equipment/{equipment}")
    public String byEquipment(@PathVariable String equipment, @PageableDefault(size = 10) Pageable pageable, Model model) {
        Page<Exercise> exercisePage = exerciseService.findByEquipment(equipment, pageable);
        model.addAttribute("exercisePage", exercisePage);
        model.addAttribute("exercises", exercisePage.getContent());
        model.addAttribute("equipment", equipment);
        model.addAttribute("bodyParts", exerciseService.findDistinctBodyParts());
        model.addAttribute("equipmentTypes", exerciseService.findDistinctEquipment());
        model.addAttribute("targets", exerciseService.findDistinctTargets());
        return "exercises";
    }

    @GetMapping("/target/{target}")
    public String byTarget(@PathVariable String target, @PageableDefault(size = 10) Pageable pageable, Model model) {
        Page<Exercise> exercisePage = exerciseService.findByTarget(target, pageable);
        model.addAttribute("exercisePage", exercisePage);
        model.addAttribute("exercises", exercisePage.getContent());
        model.addAttribute("target", target);
        model.addAttribute("bodyParts", exerciseService.findDistinctBodyParts());
        model.addAttribute("equipmentTypes", exerciseService.findDistinctEquipment());
        model.addAttribute("targets", exerciseService.findDistinctTargets());
        return "exercises";
    }
}