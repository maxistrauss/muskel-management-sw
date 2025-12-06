package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.SubscriptionDto;

public interface PayPalService {
    
    /**
     * Create a PayPal order for a subscription
     * @param subscriptionDto The subscription to pay for
     * @return The order ID from PayPal
     */
    String createOrder(SubscriptionDto subscriptionDto);
    
    /**
     * Capture/complete a PayPal order
     * @param orderId The PayPal order ID
     * @return true if successful
     */
    boolean captureOrder(String orderId);
    
    /**
     * Get the access token from PayPal API
     * @return The access token
     */
    String getAccessToken();
}
