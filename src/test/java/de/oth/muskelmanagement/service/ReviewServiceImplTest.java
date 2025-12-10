package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.ReviewDto;
import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.repository.CourseRepository;
import de.oth.muskelmanagement.repository.ReviewRepository;
import de.oth.muskelmanagement.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User member;
    private User otherMember;
    private Course course;
    private Review review;
    private ReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        member = new User();
        member.setId(1L);
        member.setFirstName("Max");

        otherMember = new User();
        otherMember.setId(2L);

        course = new Course();
        course.setId(1L);
        course.setName("Yoga");

        review = new Review(course, member, 5, "Great course!");
        review.setId(1L);

        reviewDto = new ReviewDto();
        reviewDto.setCourseId(1L);
        reviewDto.setRating(4);
        reviewDto.setComment("Pretty good");
    }

    @Test
    void createReview_ShouldSaveReview() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review created = reviewService.createReview(reviewDto, member);

        assertNotNull(created);
        assertEquals(4, created.getRating());
        assertEquals("Pretty good", created.getComment());
        assertEquals(member, created.getMember());
        assertEquals(course, created.getCourse());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateReview_ShouldUpdate_WhenOwner() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewDto.setRating(3);
        reviewDto.setComment("Updated comment");

        Review updated = reviewService.updateReview(1L, reviewDto, member);

        assertEquals(3, updated.getRating());
        assertEquals("Updated comment", updated.getComment());
    }

    @Test
    void updateReview_ShouldThrowException_WhenNotOwner() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalArgumentException.class, () -> reviewService.updateReview(1L, reviewDto, otherMember));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview_ShouldDelete_WhenOwner() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L, member);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_ShouldThrowException_WhenNotOwner() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalArgumentException.class, () -> reviewService.deleteReview(1L, otherMember));
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void getReviewsByCourse_ShouldReturnList() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(reviewRepository.findByCourseOrderByCreatedAtDesc(course)).thenReturn(List.of(review));

        List<Review> result = reviewService.getReviewsByCourse(1L);

        assertEquals(1, result.size());
        assertEquals(review, result.get(0));
    }
}
