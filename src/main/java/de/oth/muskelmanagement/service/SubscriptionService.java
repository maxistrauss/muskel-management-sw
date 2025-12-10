package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.SubscriptionDto;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {
    
    /**
     * Create a new subscription for a user
     * @param userId The user ID
     * @param pricingId The pricing ID
     * @return The created subscription DTO
     * @throws RuntimeException if user already has an active subscription or pricing is not active
     */
    SubscriptionDto subscribe(Long userId, Long pricingId);
    
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
    
    /**
     * Mark subscription as paid with PayPal order ID
     * @param subscriptionId The subscription ID
     * @param paypalOrderId The PayPal order ID
     */
    void markAsPaid(Long subscriptionId, String paypalOrderId);
    
    /**
     * Update PayPal order ID for a subscription
     * @param subscriptionId The subscription ID
     * @param paypalOrderId The PayPal order ID
     */
    void updatePayPalOrderId(Long subscriptionId, String paypalOrderId);
    
    /**
     * Mark subscription as paid by user ID and PayPal order ID (finds active subscription)
     * @param userId The user ID
     * @param paypalOrderId The PayPal order ID
     */
    void markAsPaidByUserId(Long userId, String paypalOrderId);

    // Methods returning entities directly for internal service use
    Optional<de.oth.muskelmanagement.model.entity.Subscription> findActiveSubscriptionEntity(Long userId);

    List<de.oth.muskelmanagement.model.entity.Subscription> findAllActiveSubscriptionsEntities(Long userId);
}
