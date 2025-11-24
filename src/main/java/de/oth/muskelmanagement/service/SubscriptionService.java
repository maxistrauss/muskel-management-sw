package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.service.dto.SubscriptionDto;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {
    
    /**
     * Create a new subscription for a user
     * @param userId The user ID
     * @param tarifId The tarif ID
     * @return The created subscription DTO
     * @throws RuntimeException if user already has an active subscription or tarif is not active
     */
    SubscriptionDto subscribe(Long userId, Long tarifId);
    
    /**
     * Get the active subscription for a user
     * @param userId The user ID
     * @return Optional containing the active subscription, or empty if none exists
     */
    Optional<SubscriptionDto> getActiveSubscription(Long userId);
    
    /**
     * Get subscription history for a user
     * @param userId The user ID
     * @return List of all subscriptions ordered by creation date (newest first)
     */
    List<SubscriptionDto> getUserSubscriptionHistory(Long userId);
    
    /**
     * Cancel a subscription
     * @param subscriptionId The subscription ID
     * @throws RuntimeException if subscription not found or already cancelled/expired
     */
    void cancelSubscription(Long subscriptionId);
    
    /**
     * Check if a user can subscribe (no active subscription exists)
     * @param userId The user ID
     * @return true if user can subscribe, false otherwise
     */
    boolean canSubscribe(Long userId);
    
    /**
     * Update expired subscriptions (called by scheduled task or manually)
     * Changes status from ACTIVE to EXPIRED for subscriptions past their end date
     */
    void updateExpiredSubscriptions();
    
    /**
     * Get subscription by ID
     * @param id The subscription ID
     * @return The subscription DTO
     * @throws RuntimeException if not found
     */
    SubscriptionDto getSubscriptionById(Long id);
}