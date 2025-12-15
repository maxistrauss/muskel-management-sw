package de.oth.muskelmanagement.controller.web.member;

import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Exercise;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.ReviewService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/member/course-info")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final ReviewService reviewService;

    public CourseController(CourseService courseService, UserService userService, ReviewService reviewService) {
        this.courseService = courseService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String list(Model model, @PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        var page = courseService.findAll(pageable);
        model.addAttribute("coursePage", page);
        model.addAttribute("courses", page.getContent());
        return "member/course-overview";
    }

    @GetMapping("/overview")
    public String overview(Model model, @PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        return list(model, pageable);
    }

    @GetMapping("/my")
    public String myCourses(Model model, Principal principal, @PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        if (principal == null) {
            return "redirect:/login";
        }
        var user = userService.findByEmail(principal.getName());
        var enrollmentPage = courseService.getUserEnrollments(user.getId(), pageable);
        model.addAttribute("enrollmentPage", enrollmentPage);
        model.addAttribute("currentUser", user);
        return "member/my-courses";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);

        // Get course exercises
        Set<Exercise> exercises = courseService.getExercisesByCourse(id);
        model.addAttribute("exercises", exercises);

        // Get course reviews
        List<Review> reviews = reviewService.getReviewsByCourse(id);
        model.addAttribute("reviews", reviews);

        boolean enrolled = false;
        boolean waitlisted = false;
        int waitlistPosition = -1;
        boolean courseFull = courseService.isCourseFull(id);

        if (principal != null) {
            var user = userService.findByEmail(principal.getName());
            model.addAttribute("currentUser", user);
            if (user != null) {
                var enrollment = courseService.getEnrollment(id, user.getId());
                if (enrollment != null) {
                    if (enrollment.getStatus() == de.oth.muskelmanagement.model.enums.EnrollmentStatus.CONFIRMED) {
                        enrolled = true;
                    } else if (enrollment.getStatus()
                            == de.oth.muskelmanagement.model.enums.EnrollmentStatus.WAITLISTED) {
                        waitlisted = true;
                        waitlistPosition = courseService.getWaitlistPosition(id, user.getId());
                    }
                }
            }
        }

        model.addAttribute("enrolled", enrolled);
        model.addAttribute("waitlisted", waitlisted);
        model.addAttribute("waitlistPosition", waitlistPosition);
        model.addAttribute("courseFull", courseFull);

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
            return "redirect:/member/course-info/" + id + "?error=not_member";
        }
        courseService.addMember(id, user.getId());
        return "redirect:/member/course-info/" + id;
    }

    @PostMapping("/{id}/unenroll")
    public String unenroll(@PathVariable Long id, Principal principal) {
        if (principal == null)
            return "redirect:/login";
        var user = userService.findByEmail(principal.getName());
        boolean isMember = user.getRoles().stream().anyMatch(r -> "ROLE_MEMBER".equals(r.getName()));
        if (!isMember) {
            return "redirect:/member/course-info/" + id + "?error=not_member";
        }
        courseService.removeMember(id, user.getId());
        return "redirect:/member/course-info/" + id;
    }
}
