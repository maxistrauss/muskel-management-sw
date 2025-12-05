package de.oth.muskelmanagement.controller.web.member;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.SubscriptionService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/member/subscriptions")
@PreAuthorize("hasRole('MEMBER')")
public class MemberSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public MemberSubscriptionController(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @GetMapping
    public String viewSubscriptions(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());

        // Get active subscription
        Optional<SubscriptionDto> activeSubscription = subscriptionService.getActiveSubscription(user.getId());

        // Get subscription history
        List<SubscriptionDto> subscriptionHistory = subscriptionService.getUserSubscriptionHistory(user.getId());

        model.addAttribute("activeSubscription", activeSubscription.orElse(null));
        model.addAttribute("subscriptionHistory", subscriptionHistory);
        model.addAttribute("canSubscribe", subscriptionService.canSubscribe(user.getId()));

        return "member/subscriptions";
    }

    @PostMapping("/{id}/cancel")
    public String cancelSubscription(@PathVariable Long id, Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());

            // Verify that the subscription belongs to the current user
            SubscriptionDto subscription = subscriptionService.getSubscriptionById(id);
            if (!subscription.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only cancel your own subscription");
                return "redirect:/member/subscriptions";
            }

            subscriptionService.cancelSubscription(id);
            redirectAttributes.addFlashAttribute("success",
                    "Subscription cancelled successfully. You will have access until " + subscription.getEndDate());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel subscription: " + e.getMessage());
        }

        return "redirect:/member/subscriptions";
    }
}
