package de.oth.muskelmanagement.controller.web.general;

import de.oth.muskelmanagement.dto.ReviewDto;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.ReviewService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final CourseService courseService;

    public ReviewController(ReviewService reviewService, UserService userService, CourseService courseService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.courseService = courseService;
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member/reviews")
    public String getMyReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User member = userService.findByEmail(userDetails.getUsername());
        List<Review> reviews = reviewService.getReviewsByMember(member);
        model.addAttribute("reviews", reviews);
        return "member/my-reviews";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member/course-info/{courseId}/reviews/new")
    public String createReviewForm(@PathVariable Long courseId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User member = userService.findByEmail(userDetails.getUsername());

        // Security check: Ensure member is enrolled in the course
        boolean isEnrolled = courseService.listEnrollments(courseId).stream()
                .anyMatch(enrollment -> enrollment.getUser().getId().equals(member.getId()));

        if (!isEnrolled) {
            throw new org.springframework.security.access.AccessDeniedException("You can only review courses you are enrolled in.");
        }

        ReviewDto dto = new ReviewDto();
        dto.setCourseId(courseId);

        // Add course name for display
        dto.setCourseName(courseService.findById(courseId).getName());

        model.addAttribute("review", dto);
        return "member/review-form";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/member/course-info/{courseId}/reviews")
    public String createReview(@PathVariable Long courseId, @ModelAttribute ReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User member = userService.findByEmail(userDetails.getUsername());
        reviewDto.setCourseId(courseId);
        reviewService.createReview(reviewDto, member);
        return "redirect:/member/reviews";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member/reviews/{id}/edit")
    public String editReviewForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Review review = reviewService.getReviewById(id);
        User member = userService.findByEmail(userDetails.getUsername());

        // Security Check
        if (!review.getMember().getId().equals(member.getId())) {
            return "redirect:/member/reviews?error=access_denied";
        }

        ReviewDto dto = reviewService.toDto(review);
        model.addAttribute("review", dto);
        model.addAttribute("isEdit", true);
        return "member/review-form";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/member/reviews/{id}")
    public String updateReview(@PathVariable Long id, @ModelAttribute ReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User member = userService.findByEmail(userDetails.getUsername());
        reviewService.updateReview(id, reviewDto, member);
        return "redirect:/member/reviews";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/member/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User member = userService.findByEmail(userDetails.getUsername());
        reviewService.deleteReview(id, member);
        return "redirect:/member/reviews";
    }
}
