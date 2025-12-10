package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Course;
import de.oth.muskelmanagement.model.entity.Review;
import de.oth.muskelmanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMemberOrderByCreatedAtDesc(User member);

    List<Review> findByCourseOrderByCreatedAtDesc(Course course);
}
