package de.oth.muskelmanagement.controller.web.trainer;

import de.oth.muskelmanagement.dto.UserDto;
import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.ReviewService;
import de.oth.muskelmanagement.service.RoomService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/trainer")
public class TrainerController {

    private final CourseService courseService;
    private final UserService userService;
    private final RoomService roomService;
    private final ReviewService reviewService;

    public TrainerController(CourseService courseService, UserService userService, RoomService roomService,
            ReviewService reviewService) {
        this.courseService = courseService;
        this.userService = userService;
        this.roomService = roomService;
        this.reviewService = reviewService;
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
    public String createCourse(@ModelAttribute Course course, Principal principal) {
        var trainer = userService.findByEmail(principal.getName());
        course.setTrainer(trainer);
        courseService.save(course);
        return "redirect:/trainer/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.findById(id);
        // Check if current user is the trainer of this course
        var currentTrainer = userService.findByEmail(principal.getName());
        if (course.getTrainer() != null && !course.getTrainer().getId().equals(currentTrainer.getId())) {
            throw new AccessDeniedException("You can only edit your own courses");
        }
        model.addAttribute("course", course);
        model.addAttribute("currentTrainer", currentTrainer);
        var rooms = roomService.findActiveRooms();
        model.addAttribute("rooms", rooms);
        return "trainer/course-form";
    }

    @PostMapping("/courses/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course courseForm, Principal principal) {
        Course existing = courseService.findById(id);
        // Check if current user is the trainer of this course
        var currentTrainer = userService.findByEmail(principal.getName());
        if (existing.getTrainer() != null && !existing.getTrainer().getId().equals(currentTrainer.getId())) {
            throw new AccessDeniedException("You can only edit your own courses");
        }
        existing.setName(courseForm.getName());
        existing.setDescription(courseForm.getDescription());
        existing.setCapacity(courseForm.getCapacity());
        existing.setActive(courseForm.isActive());
        existing.setRoom(courseForm.getRoom());
        existing.setStartDate(courseForm.getStartDate());
        existing.setEndDate(courseForm.getEndDate());
        existing.setDaysOfWeek(courseForm.getDaysOfWeek());
        courseService.save(existing);
        return "redirect:/trainer/courses";
    }

    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Principal principal) {
        Course course = courseService.findById(id);
        // Check if current user is the trainer of this course
        var currentTrainer = userService.findByEmail(principal.getName());
        if (course.getTrainer() != null && !course.getTrainer().getId().equals(currentTrainer.getId())) {
            throw new AccessDeniedException("You can only delete your own courses");
        }
        courseService.deleteById(id);
        return "redirect:/trainer/courses";
    }

    @GetMapping("/courses")
    public String listCourses(Model model, @PageableDefault(size = 10) Pageable pageable) {
        var page = courseService.findAll(pageable);
        model.addAttribute("coursePage", page);
        model.addAttribute("courses", page.getContent());
        return "trainer/courses";
    }

    @GetMapping("/courses/{id}/participants")
    public String viewParticipants(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        List<Enrollment> enrollments = courseService.listEnrollments(id);
        model.addAttribute("course", course);
        model.addAttribute("enrollments", enrollments);
        return "trainer/course-participants";
    }

    @GetMapping("/courses/{id}/reviews")
    public String viewReviews(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        List<Review> reviews = reviewService.getReviewsByCourse(id);
        model.addAttribute("course", course);
        model.addAttribute("reviews", reviews);
        return "trainer/course-reviews";
    }

    @GetMapping("/courses/{id}/add-member")
    public String showAddMemberForm(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        Page<UserDto> allUsers = userService.findAll(Pageable.unpaged());
        // Filter out already enrolled users
        List<Enrollment> currentEnrollments = courseService.listEnrollments(id);
        var enrolledUserIds = currentEnrollments.stream().map(e -> e.getUser().getId()).toList();
        var availableUsers = allUsers.getContent().stream().filter(u -> !enrolledUserIds.contains(u.getId())).toList();
        model.addAttribute("course", course);
        model.addAttribute("availableUsers", availableUsers);
        return "trainer/add-member";
    }

    @PostMapping("/courses/{id}/add-member")
    public String addMember(@PathVariable Long id, @RequestParam Long userId) {
        courseService.addMember(id, userId);
        return "redirect:/trainer/courses/{id}/participants";
    }

    @PostMapping("/courses/{id}/remove-member/{userId}")
    public String removeMember(@PathVariable Long id, @PathVariable Long userId) {
        courseService.removeMember(id, userId);
        return "redirect:/trainer/courses/{id}/participants";
    }

    @GetMapping("/members")
    public String listMembers(Model model, @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDto> users = userService.findAll(pageable);
        model.addAttribute("userPage", users);
        return "trainer/members";
    }
}
