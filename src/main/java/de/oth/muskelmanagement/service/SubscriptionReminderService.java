package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.entity.Subscription;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import de.oth.muskelmanagement.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SubscriptionReminderService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionReminderService.class);
    private static final int REMINDER_DAYS_BEFORE_EXPIRY = 7;

    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    public SubscriptionReminderService(SubscriptionRepository subscriptionRepository, EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
    }

    /**
     * Scheduled task that runs daily at 9:00 AM to send expiry reminders
     * Cron format: second, minute, hour, day, month, weekday
     * "0 0 9 * * ?" = Every day at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendExpiryReminders() {
        logger.info("Starting scheduled task: sendExpiryReminders");

        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(REMINDER_DAYS_BEFORE_EXPIRY);

        // Find all active subscriptions expiring within the next 7 days that haven't received a reminder
        List<Subscription> subscriptionsToRemind = subscriptionRepository
                .findByStatusAndEndDateBetweenAndReminderSent(
                        SubscriptionStatus.ACTIVE,
                        today,
                        reminderDate,
                        false
                );

        logger.info("Found {} subscriptions requiring expiry reminders", subscriptionsToRemind.size());

        int successCount = 0;
        int failureCount = 0;

        for (Subscription subscription : subscriptionsToRemind) {
            try {
                // Calculate days remaining
                long daysRemaining = ChronoUnit.DAYS.between(today, subscription.getEndDate());

                // Send reminder email
                String userName = subscription.getUser().getFirstName() + " " + subscription.getUser().getLastName();
                String userEmail = subscription.getUser().getEmail();
                String tarifName = subscription.getTarif().getName();

                emailService.sendSubscriptionExpiryReminder(
                        userEmail,
                        userName,
                        tarifName,
                        subscription.getEndDate(),
                        daysRemaining
                );

                // Mark reminder as sent
                subscription.setReminderSent(true);
                subscriptionRepository.save(subscription);

                successCount++;
                logger.info("Sent expiry reminder to user {} for subscription {}", userEmail, subscription.getId());

            } catch (Exception e) {
                failureCount++;
                logger.error("Failed to send expiry reminder for subscription {}: {}", 
                        subscription.getId(), e.getMessage(), e);
            }
        }

        logger.info("Expiry reminder task completed: {} successful, {} failed", successCount, failureCount);
    }

    /**
     * Manual trigger for testing purposes
     * Can be called by admin or for immediate processing
     */
    @Transactional
    public void sendExpiryRemindersManually() {
        logger.info("Manually triggering expiry reminders");
        sendExpiryReminders();
    }
}