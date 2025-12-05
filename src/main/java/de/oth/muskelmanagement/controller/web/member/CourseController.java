package de.oth.muskelmanagement.controller.web.member;

import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Exercise;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Set;

@Controller
@RequestMapping("/member/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model, @PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        var page = courseService.findAll(pageable);
        model.addAttribute("coursePage", page);
        model.addAttribute("courses", page.getContent());
        return "courses";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);

        // Get course exercises
        Set<Exercise> exercises = courseService.getExercisesByCourse(id);
        model.addAttribute("exercises", exercises);

        boolean enrolled = false;
        if (principal != null) {
            var user = userService.findByEmail(principal.getName());
            if (user != null) {
                var enrolls = courseService.listEnrollments(id);
                enrolled = enrolls.stream().anyMatch(e -> e.getUser().getId().equals(user.getId()));
            }
        }
        model.addAttribute("enrolled", enrolled);
        return "course-details";
    }

    @PostMapping("/{id}/enroll")
    public String enroll(@PathVariable Long id, Principal principal) {
        if (principal == null)
            return "redirect:/login";
        var user = userService.findByEmail(principal.getName());
        // Only users with ROLE_MEMBER may enroll themselves
        boolean isMember = user.getRoles().stream().anyMatch(r -> "ROLE_MEMBER".equals(r.getName()));
        if (!isMember) {
            return "redirect:/member/courses/" + id + "?error=not_member";
        }
        courseService.addMember(id, user.getId());
        return "redirect:/member/courses/" + id;
    }

    @PostMapping("/{id}/unenroll")
    public String unenroll(@PathVariable Long id, Principal principal) {
        if (principal == null)
            return "redirect:/login";
        var user = userService.findByEmail(principal.getName());
        boolean isMember = user.getRoles().stream().anyMatch(r -> "ROLE_MEMBER".equals(r.getName()));
        if (!isMember) {
            return "redirect:/member/courses/" + id + "?error=not_member";
        }
        courseService.removeMember(id, user.getId());
        return "redirect:/member/courses/" + id;
    }
}
