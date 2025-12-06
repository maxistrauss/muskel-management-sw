package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.ReviewDto;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.model.entity.User;

import java.util.List;

public interface ReviewService {
    Review createReview(ReviewDto dto, User member);

    Review updateReview(Long id, ReviewDto dto, User member);

    void deleteReview(Long id, User member);

    List<Review> getReviewsByMember(User member);

    List<Review> getReviewsByCourse(Long courseId);

    Review getReviewById(Long id);

    ReviewDto toDto(Review review);
}
