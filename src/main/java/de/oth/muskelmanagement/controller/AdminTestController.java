package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.model.Subscription;
import de.oth.muskelmanagement.model.SubscriptionStatus;
import de.oth.muskelmanagement.model.Tarif;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.repository.SubscriptionRepository;
import de.oth.muskelmanagement.repository.TarifRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.SubscriptionReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test controller for testing subscription reminder functionality.
 * Only accessible by ADMIN users.
 * WARNING: This controller should only be used in development/testing environments!
 */
@RestController
@RequestMapping("/api/admin/test")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTestController {

    private static final Logger logger = LoggerFactory.getLogger(AdminTestController.class);

    private final SubscriptionReminderService reminderService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final TarifRepository tarifRepository;

    public AdminTestController(SubscriptionReminderService reminderService,
                              SubscriptionRepository subscriptionRepository,
                              UserRepository userRepository,
                              TarifRepository tarifRepository) {
        this.reminderService = reminderService;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.tarifRepository = tarifRepository;
    }

    /**
     * Manually trigger the subscription expiry reminder check.
     * This bypasses the scheduled cron job and runs immediately.
     * 
     * @return Response with execution details
     */
    @PostMapping("/trigger-reminders")
    public ResponseEntity<Map<String, Object>> triggerReminders() {
        logger.info("Manual reminder trigger requested by admin");
        
        Map<String, Object> response = new HashMap<>();
        try {
            // Get stats before triggering
            LocalDate today = LocalDate.now();
            LocalDate reminderDate = today.plusDays(7);
            
            List<Subscription> eligibleSubscriptions = subscriptionRepository
                .findByStatusAndEndDateBetweenAndReminderSent(
                    SubscriptionStatus.ACTIVE,
                    today,
                    reminderDate,
                    false
                );
            
            response.put("eligibleSubscriptionsCount", eligibleSubscriptions.size());
            response.put("checkDate", today.toString());
            response.put("reminderWindow", today + " to " + reminderDate);
            
            // Trigger the reminder service
            reminderService.sendExpiryRemindersManually();
            
            response.put("status", "success");
            response.put("message", "Reminder check completed successfully");
            logger.info("Manual reminder trigger completed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during manual reminder trigger", e);
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Create a test subscription with a custom expiry date.
     * This allows testing the reminder system without waiting for natural expiration.
     * 
     * @param userId User ID to create subscription for
     * @param tarifId Tarif ID to use
     * @param daysUntilExpiry Number of days until subscription expires
     * @return Response with created subscription details
     */
    @PostMapping("/create-test-subscription")
    public ResponseEntity<Map<String, Object>> createTestSubscription(
            @RequestParam Long userId,
            @RequestParam Long tarifId,
            @RequestParam(defaultValue = "5") Integer daysUntilExpiry) {
        
        logger.info("Creating test subscription for user {} with tarif {}, expiring in {} days", 
                    userId, tarifId, daysUntilExpiry);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            
            // Validate tarif
            Tarif tarif = tarifRepository.findById(tarifId)
                .orElseThrow(() -> new RuntimeException("Tarif not found with id: " + tarifId));
            
            // Check if user already has an active subscription
            if (subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
                response.put("status", "error");
                response.put("message", "User already has an active subscription. Cancel it first.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create test subscription with custom dates
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(daysUntilExpiry);
            
            Subscription testSubscription = new Subscription();
            testSubscription.setUser(user);
            testSubscription.setTarif(tarif);
            testSubscription.setStartDate(startDate);
            testSubscription.setEndDate(endDate);
            testSubscription.setStatus(SubscriptionStatus.ACTIVE);
            testSubscription.setAutoRenew(false);
            testSubscription.setReminderSent(false);
            
            Subscription saved = subscriptionRepository.save(testSubscription);
            
            response.put("status", "success");
            response.put("message", "Test subscription created successfully");
            response.put("subscriptionId", saved.getId());
            response.put("userId", userId);
            response.put("userEmail", user.getEmail());
            response.put("userName", user.getFirstName() + " " + user.getLastName());
            response.put("tarifName", tarif.getName());
            response.put("startDate", startDate.toString());
            response.put("endDate", endDate.toString());
            response.put("daysUntilExpiry", daysUntilExpiry);
            response.put("withinReminderWindow", daysUntilExpiry <= 7);
            
            logger.info("Test subscription created successfully: {}", saved.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating test subscription", e);
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all active subscriptions and their reminder eligibility status.
     * Useful for debugging and understanding which subscriptions will receive reminders.
     * 
     * @return List of active subscriptions with reminder information
     */
    @GetMapping("/subscription-status")
    public ResponseEntity<Map<String, Object>> getSubscriptionStatus() {
        logger.info("Fetching subscription status for reminder testing");
        
        Map<String, Object> response = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(7);
        
        // Get all active subscriptions
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        
        // Get eligible subscriptions (within reminder window and not already sent)
        List<Subscription> eligibleSubscriptions = subscriptionRepository
            .findByStatusAndEndDateBetweenAndReminderSent(
                SubscriptionStatus.ACTIVE,
                today,
                reminderDate,
                false
            );
        
        response.put("currentDate", today.toString());
        response.put("reminderWindow", today + " to " + reminderDate);
        response.put("totalActiveSubscriptions", activeSubscriptions.size());
        response.put("eligibleForReminder", eligibleSubscriptions.size());
        
        // Add details about eligible subscriptions
        List<Map<String, Object>> eligibleDetails = eligibleSubscriptions.stream()
            .map(sub -> {
                Map<String, Object> details = new HashMap<>();
                details.put("subscriptionId", sub.getId());
                details.put("userId", sub.getUser().getId());
                details.put("userEmail", sub.getUser().getEmail());
                details.put("userName", sub.getUser().getFirstName() + " " + sub.getUser().getLastName());
                details.put("tarifName", sub.getTarif().getName());
                details.put("endDate", sub.getEndDate().toString());
                details.put("daysRemaining", java.time.temporal.ChronoUnit.DAYS.between(today, sub.getEndDate()));
                details.put("reminderSent", sub.getReminderSent());
                return details;
            })
            .toList();
        
        response.put("eligibleSubscriptionDetails", eligibleDetails);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Reset the reminder_sent flag for a specific subscription.
     * Useful for re-testing the reminder on the same subscription.
     * 
     * @param subscriptionId Subscription ID to reset
     * @return Response with reset status
     */
    @PostMapping("/reset-reminder-flag/{subscriptionId}")
    public ResponseEntity<Map<String, Object>> resetReminderFlag(@PathVariable Long subscriptionId) {
        logger.info("Resetting reminder flag for subscription: {}", subscriptionId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));
            
            boolean previousValue = subscription.getReminderSent();
            subscription.setReminderSent(false);
            subscriptionRepository.save(subscription);
            
            response.put("status", "success");
            response.put("message", "Reminder flag reset successfully");
            response.put("subscriptionId", subscriptionId);
            response.put("previousValue", previousValue);
            response.put("newValue", false);
            
            logger.info("Reminder flag reset successfully for subscription: {}", subscriptionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error resetting reminder flag", e);
            response.put("status", "error");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}