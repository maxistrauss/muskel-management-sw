package de.oth.muskelmanagement.controller.web.member;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.PayPalService;
import de.oth.muskelmanagement.service.SubscriptionService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/member/payment")
@PreAuthorize("hasRole('MEMBER')")
public class MemberPaymentController {

    private final PayPalService payPalService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

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
            
            // Create PayPal order (generates order ID)
            String orderId = payPalService.createOrder(subscription);
            
            if (orderId != null) {
                // Redirect to PayPal checkout
                // In a real scenario with API, this would be the approval URL from PayPal
                // For demo: redirect to success page and mark as paid
                subscriptionService.markAsPaid(subscriptionId, orderId);
                redirectAttributes.addFlashAttribute("success", "Payment successful!");
                return "redirect:/member/subscriptions";
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create payment");
                return "redirect:/member/subscriptions";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/subscriptions";
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(required = false) String orderId, 
                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "Payment completed successfully!");
        return "redirect:/member/subscriptions";
    }

    @GetMapping("/cancel")
    public String paymentCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Payment cancelled");
        return "redirect:/member/subscriptions";
    }
}
