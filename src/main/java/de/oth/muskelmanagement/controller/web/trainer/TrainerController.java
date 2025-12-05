package de.oth.muskelmanagement.controller.web.trainer;

import de.oth.muskelmanagement.dto.UserDto;
import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Enrollment;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/trainer")
public class TrainerController {

    private final CourseService courseService;
    private final UserService userService;

    public TrainerController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
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
}
