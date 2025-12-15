package de.oth.muskelmanagement.controller.web.trainer;

import de.oth.muskelmanagement.dto.UserDto;
import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.model.entity.Exercise;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/trainer")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);
    private final CourseService courseService;
    private final UserService userService;
    private final RoomService roomService;
    private final ReviewService reviewService;
    private final ExerciseService exerciseService;

    public TrainerController(CourseService courseService, UserService userService, RoomService roomService,
                             ReviewService reviewService, ExerciseService exerciseService) {
        this.courseService = courseService;
        this.userService = userService;
        this.roomService = roomService;
        this.reviewService = reviewService;
        this.exerciseService = exerciseService;
    }

    @GetMapping("/courses/new")
    public String showCreateCourseForm(Model model, Principal principal) {
        model.addAttribute("course", new Course());
        var currentUser = userService.findByEmail(principal.getName());
        model.addAttribute("currentTrainer", currentUser);
        var rooms = roomService.findActiveRooms();
        model.addAttribute("rooms", rooms);
        return "trainer/course-form";
    }

    @PostMapping("/courses/new")
    public String createCourse(@Valid @ModelAttribute("course") Course course, BindingResult bindingResult, Principal principal, Model model) {
        var trainer = userService.findByEmail(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentTrainer", trainer);
            model.addAttribute("rooms", roomService.findActiveRooms());
            return "trainer/course-form";
        }
        course.setTrainer(trainer);
        Course newCourse = courseService.save(course);
        courseService.addMember(newCourse.getId(), trainer.getId());
        return "redirect:/trainer/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.findById(id);
        var currentTrainer = userService.findByEmail(principal.getName());
        boolean isOwner = course.getTrainer() != null && course.getTrainer().getId().equals(currentTrainer.getId());
        boolean isAdmin = currentTrainer.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only edit your own courses");
        }
        model.addAttribute("course", course);
        model.addAttribute("currentTrainer", currentTrainer);
        var rooms = roomService.findActiveRooms();
        model.addAttribute("rooms", rooms);

        // Member Management Data
        List<UserDto> enrolledMembers = courseService.listEnrollments(id).stream()
                .map(Enrollment::getUser)
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setEmail(user.getEmail());
                    return dto;
                })
                .collect(Collectors.toList());

        Set<Long> enrolledMemberIds = enrolledMembers.stream()
                .map(UserDto::getId)
                .collect(Collectors.toSet());

        List<UserDto> availableMembers = userService.findAll(Pageable.unpaged()).getContent().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.equals("ROLE_MEMBER")) && !enrolledMemberIds.contains(user.getId()))
                .collect(Collectors.toList());

        model.addAttribute("enrolledMembers", enrolledMembers);
        model.addAttribute("availableMembers", availableMembers);

        return "trainer/course-form";
    }

    @PostMapping("/courses/edit/{id}")
    public String updateCourse(@PathVariable Long id, @Valid @ModelAttribute("course") Course course, BindingResult bindingResult, Principal principal, Model model) {
        Course existing = courseService.findById(id);
        var currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = existing.getTrainer() != null && existing.getTrainer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only edit your own courses");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentTrainer", currentUser);
            model.addAttribute("rooms", roomService.findActiveRooms());
            List<UserDto> enrolledMembers = courseService.listEnrollments(id).stream()
                    .map(Enrollment::getUser)
                    .map(user -> {
                        UserDto dto = new UserDto();
                        dto.setId(user.getId());
dto.setFirstName(user.getFirstName());
                        dto.setLastName(user.getLastName());
                        dto.setEmail(user.getEmail());
                        return dto;
                    })
                    .collect(Collectors.toList());
            model.addAttribute("enrolledMembers", enrolledMembers);

            Set<Long> enrolledMemberIds = enrolledMembers.stream().map(UserDto::getId).collect(Collectors.toSet());
            List<UserDto> availableMembers = userService.findAll(Pageable.unpaged()).getContent().stream()
                    .filter(user -> user.getRoles().stream().anyMatch(role -> role.equals("ROLE_MEMBER")) && !enrolledMemberIds.contains(user.getId()))
                    .collect(Collectors.toList());
            model.addAttribute("availableMembers", availableMembers);

            // Preserve exercises and trainer info when returning to form
            course.setExercises(existing.getExercises());
            course.setTrainer(existing.getTrainer());

            return "trainer/course-form";
        }

        existing.setName(course.getName());
        existing.setDescription(course.getDescription());
        existing.setCapacity(course.getCapacity());
        existing.setActive(course.isActive());
        existing.setRoom(course.getRoom());
        existing.setStartDate(course.getStartDate());
        existing.setEndDate(course.getEndDate());
        existing.setStartTime(course.getStartTime());
        existing.setDurationMinutes(course.getDurationMinutes());
        existing.setDaysOfWeek(course.getDaysOfWeek());
        courseService.save(existing);
        return "redirect:/trainer/courses";
    }

    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Principal principal) {
        Course course = courseService.findById(id);
        var currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = course.getTrainer() != null && course.getTrainer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only delete your own courses");
        }
        courseService.deleteById(id);
        return "redirect:/trainer/courses";
    }

    @GetMapping("/courses")
    public String listCourses(Model model, @PageableDefault(size = 10) Pageable pageable, Principal principal) {
        var trainer = userService.findByEmail(principal.getName());
        var page = courseService.findByTrainerId(trainer.getId(), pageable);
        model.addAttribute("coursePage", page);
        return "trainer/courses";
    }

    @GetMapping("/courses/{id}/info")
    public String showCourseInfo(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.findById(id);
        var currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = course.getTrainer() != null && course.getTrainer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to view this course info.");
        }

        List<Review> reviews = reviewService.getReviewsByCourse(id);
        model.addAttribute("course", course);
        model.addAttribute("reviews", reviews);
        model.addAttribute("exercises", course.getExercises());

        return "trainer/course-info";
    }

    @PostMapping("/courses/edit/{id}/add-member")
    public String addMemberToCourse(@PathVariable Long id, @RequestParam Long memberId, Principal principal) {
        Course course = courseService.findById(id);
        var currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = course.getTrainer() != null && course.getTrainer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to manage members for this course.");
        }

        courseService.addMember(id, memberId);
        return "redirect:/trainer/courses/edit/" + id;
    }

    @PostMapping("/courses/edit/{id}/remove-member/{memberId}")
    public String removeMemberFromCourse(@PathVariable Long id, @PathVariable Long memberId, Principal principal) {
        Course course = courseService.findById(id);
        var currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = course.getTrainer() != null && course.getTrainer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to manage members for this course.");
        }

        courseService.removeMember(id, memberId);
        return "redirect:/trainer/courses/edit/" + id;
    }

    @GetMapping("/members")
    public String listMembers(Model model, @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDto> users = userService.findAll(pageable);
        model.addAttribute("userPage", users);
        return "trainer/members";
    }

    @GetMapping("/courses/{id}/exercises")
    public String manageCourseExercises(@PathVariable Long id, Model model, Principal principal,
                                        @RequestParam(required = false) String bodyPart, @RequestParam(required = false) String equipment,
                                        @RequestParam(required = false) String target) {
        Course course = courseService.findById(id);
        var currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = course.getTrainer() != null && course.getTrainer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only manage exercises for your own courses");
        }

        model.addAttribute("course", course);

        // Get all exercises with optional filtering
        List<Exercise> exercises;
        if (bodyPart != null && !bodyPart.isEmpty()) {
            exercises = exerciseService.findByBodyPart(bodyPart);
        } else if (equipment != null && !equipment.isEmpty()) {
            exercises = exerciseService.findByEquipment(equipment);
        } else if (target != null && !target.isEmpty()) {
            exercises = exerciseService.findByTarget(target);
        } else {
            exercises = exerciseService.findAll();
        }

        // Get IDs of exercises already assigned to this course
        Set<Long> courseExerciseIds = course.getExercises().stream().map(Exercise::getId).collect(Collectors.toSet());

        model.addAttribute("exercises", exercises != null ? exercises : List.of());
        model.addAttribute("courseExerciseIds", courseExerciseIds);

        // Add filter options - ensure they are never null
        List<String> bodyParts = exerciseService.findDistinctBodyParts();
        List<String> equipmentOptions = exerciseService.findDistinctEquipment();
        List<String> targets = exerciseService.findDistinctTargets();

        model.addAttribute("bodyParts", bodyParts != null ? bodyParts : List.of());
        model.addAttribute("equipmentList", equipmentOptions != null ? equipmentOptions : List.of());
        model.addAttribute("targets", targets != null ? targets : List.of());

        // Add selected filter values back to model
        model.addAttribute("selectedBodyPart", bodyPart != null ? bodyPart : "");
        model.addAttribute("selectedEquipment", equipment != null ? equipment : "");
        model.addAttribute("selectedTarget", target != null ? target : "");

        return "trainer/course-exercises";
    }

    @PostMapping("/courses/{id}/exercises")
    public String saveCourseExercises(@PathVariable Long id, @RequestParam(required = false) List<Long> exerciseIds,
                                      RedirectAttributes redirectAttributes, Principal principal) {
        Course course = courseService.findById(id);
        var currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = course.getTrainer() != null && course.getTrainer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only manage exercises for your own courses");
        }

        try {
            courseService.bulkAssignExercises(id, exerciseIds != null ? exerciseIds : List.of());
            redirectAttributes.addFlashAttribute("successMessage", "Exercises successfully assigned!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error assigning exercises: " + e.getMessage());
        }
        return "redirect:/trainer/courses/edit/" + id;
    }
}
