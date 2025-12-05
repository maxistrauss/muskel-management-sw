package de.oth.muskelmanagement.controller.web.member;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.dto.TarifDto;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.SubscriptionService;
import de.oth.muskelmanagement.service.TarifService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/member/tarifs")
@PreAuthorize("hasRole('MEMBER')")
public class MemberTarifController {

    private final TarifService tarifService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public MemberTarifController(TarifService tarifService, SubscriptionService subscriptionService,
            UserService userService) {
        this.tarifService = tarifService;
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @GetMapping
    public String viewTarifs(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());

        // Get all active tarifs (use large page size to show all)
        Pageable pageable = PageRequest.of(0, 100);
        Page<TarifDto> tarifPage = tarifService.findTarifs(null, true, pageable);

        // Get user's active subscription to highlight current tarif
        Optional<SubscriptionDto> activeSubscription = subscriptionService.getActiveSubscription(user.getId());

        model.addAttribute("tarifs", tarifPage.getContent());
        model.addAttribute("activeSubscription", activeSubscription.orElse(null));
        model.addAttribute("canSubscribe", subscriptionService.canSubscribe(user.getId()));

        return "member/tarifs";
    }

    @PostMapping("/{tarifId}/subscribe")
    public String subscribe(@PathVariable Long tarifId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());

            // Check if user can subscribe
            if (!subscriptionService.canSubscribe(user.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "You already have an active subscription. Please cancel it first before subscribing to a new plan.");
                return "redirect:/member/tarifs";
            }

            // Create subscription
            SubscriptionDto subscription = subscriptionService.subscribe(user.getId(), tarifId);

            redirectAttributes.addFlashAttribute("success",
                    "Successfully subscribed to " + subscription.getTarifName() + "! Your subscription is valid until "
                            + subscription.getEndDate());

            return "redirect:/member/subscriptions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to subscribe: " + e.getMessage());
            return "redirect:/member/tarifs";
        }
    }
}
