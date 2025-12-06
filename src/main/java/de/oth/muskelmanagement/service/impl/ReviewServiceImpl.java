package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.ReviewDto;
import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.repository.CourseRepository;
import de.oth.muskelmanagement.repository.ReviewRepository;
import de.oth.muskelmanagement.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, CourseRepository courseRepository) {
        this.reviewRepository = reviewRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public Review createReview(ReviewDto dto, User member) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        Review review = new Review(course, member, dto.getRating(), dto.getComment());
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(Long id, ReviewDto dto, User member) {
        Review review = getReviewById(id);

        // Ensure user owns the review
        if (!review.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("Access denied: You can only edit your own reviews.");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, User member) {
        Review review = getReviewById(id);

        // Ensure user owns the review
        if (!review.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("Access denied: You can only delete your own reviews.");
        }

        reviewRepository.delete(review);
    }

    @Override
    public List<Review> getReviewsByMember(User member) {
        return reviewRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    @Override
    public List<Review> getReviewsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return reviewRepository.findByCourseOrderByCreatedAtDesc(course);
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    @Override
    public ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setCourseId(review.getCourse().getId());
        dto.setCourseName(review.getCourse().getName());
        dto.setMemberId(review.getMember().getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
