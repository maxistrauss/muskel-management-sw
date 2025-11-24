package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.service.SubscriptionReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/subscriptions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final SubscriptionReminderService reminderService;

    public AdminSubscriptionController(SubscriptionReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * Manual trigger for sending expiry reminder emails
     * Useful for testing or immediate processing
     * POST /api/admin/subscriptions/send-reminders
     */
    @PostMapping("/send-reminders")
    public ResponseEntity<Map<String, String>> sendExpiryReminders() {
        try {
            reminderService.sendExpiryRemindersManually();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Expiry reminders sent successfully. Check logs for details.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to send expiry reminders: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}