package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Subscription;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // Find active subscription for a user
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    // Prefer single-result retrieval: latest active subscription
    Optional<Subscription> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SubscriptionStatus status);
    
    // Find all subscriptions for a user (history) - ordered by creation date descending
    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find subscriptions expiring soon (for auto-renewal or notifications)
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);
    
    // Check if user has active subscription
    boolean existsByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    // Find all active subscriptions
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    // Find active subscriptions expiring within X days that haven't received a reminder
    List<Subscription> findByStatusAndEndDateBetweenAndReminderSent(
        SubscriptionStatus status, LocalDate startDate, LocalDate endDate, Boolean reminderSent);
}