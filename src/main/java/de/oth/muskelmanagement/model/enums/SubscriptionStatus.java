package de.oth.muskelmanagement.model.enums;

public enum SubscriptionStatus {
    PENDING,    // Subscription created but not yet active
    ACTIVE,     // Currently active subscription
    EXPIRED,    // Subscription has ended
    CANCELLED   // Subscription was cancelled before expiry
}