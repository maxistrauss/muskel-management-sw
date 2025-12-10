package de.oth.muskelmanagement.controller.web.member;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.PayPalService;
import de.oth.muskelmanagement.service.SubscriptionService;
import de.oth.muskelmanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/member/payment")
@PreAuthorize("hasRole('MEMBER')")
public class MemberPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(MemberPaymentController.class);

    private final PayPalService payPalService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    
    @Value("${paypal.mode}")
    private String mode;

    public MemberPaymentController(PayPalService payPalService, SubscriptionService subscriptionService, 
                                   UserService userService) {
        this.payPalService = payPalService;
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @GetMapping("/pay/{subscriptionId}")
    public String paySubscription(@PathVariable Long subscriptionId, Principal principal, 
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());
            SubscriptionDto subscription = subscriptionService.getSubscriptionById(subscriptionId);
            
            // Verify subscription belongs to user
            if (!subscription.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized");
                return "redirect:/member/subscriptions";
            }
            
            // Create PayPal order
            String orderId = payPalService.createOrder(subscription);
            
            if (orderId != null) {
                // Save order ID to subscription for later reference
                //subscriptionService.updatePayPalOrderId(subscriptionId, orderId);
                
                // Redirect to PayPal checkout page
                String paypalRedirectUrl = "sandbox".equalsIgnoreCase(mode)
                    ? "https://www.sandbox.paypal.com/checkoutnow?token=" + orderId
                    : "https://www.paypal.com/checkoutnow?token=" + orderId;
                
                logger.info("Redirecting to PayPal checkout: {}", paypalRedirectUrl);
                return "redirect:" + paypalRedirectUrl;
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create payment");
                return "redirect:/member/subscriptions";
            }
        } catch (Exception e) {
            logger.error("Error creating payment", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/subscriptions";
        }
    }

    @GetMapping("/return")
    public String paymentReturn(@RequestParam String token, Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());
            
            // Capture the order
            boolean success = payPalService.captureOrder(token);
            
            if (success) {
                // Find and update subscription
                subscriptionService.markAsPaid(user.getId(), token);
                redirectAttributes.addFlashAttribute("success", "Payment completed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to complete payment");
            }
        } catch (Exception e) {
            logger.error("Error processing payment return", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/member/subscriptions";
    }

    @GetMapping("/cancel")
    public String paymentCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Payment cancelled");
        return "redirect:/member/subscriptions";
    }
}
