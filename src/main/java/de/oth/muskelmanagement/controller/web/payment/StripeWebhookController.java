package de.oth.muskelmanagement.controller.web.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.Stripe;
import com.stripe.param.checkout.SessionRetrieveParams;
import com.stripe.model.LineItem;
import com.stripe.model.LineItemCollection;
import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.model.entity.Pricing;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.PricingService;
import de.oth.muskelmanagement.service.SubscriptionService;
import de.oth.muskelmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;

@Controller
@RequestMapping("/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final UserService userService;
    private final PricingService pricingService;
    private final SubscriptionService subscriptionService;

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    public StripeWebhookController(UserService userService, PricingService pricingService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.pricingService = pricingService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader,
                                                HttpServletRequest request) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            String type = event.getType();
            log.info("Stripe event received: {}", type);

            if ("checkout.session.completed".equals(type)) {
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    Session session = (Session) dataObjectDeserializer.getObject().get();

                    // Initialize Stripe SDK for follow-up API calls
                    Stripe.apiKey = stripeSecretKey;

                    // Retrieve line items to get price IDs
                    SessionRetrieveParams params = SessionRetrieveParams.builder()
                            .addExpand("line_items")
                            .build();
                    Session fullSession = Session.retrieve(session.getId(), params, null);
                    LineItemCollection items = fullSession.getLineItems();

                    String customerEmail = fullSession.getCustomerDetails() != null ? fullSession.getCustomerDetails().getEmail() : null;
                    if (customerEmail == null || customerEmail.isBlank()) {
                        log.warn("checkout.session.completed without customer email; cannot match user.");
                        return ResponseEntity.ok("ignored");
                    }
                    User user = userService.findByEmail(customerEmail);
                    if (user == null) {
                        log.warn("No local user found for email {}", customerEmail);
                        return ResponseEntity.ok("ignored");
                    }

                    // Assume single line item mapping to a plan
                    final String stripePriceId = (items != null && items.getData() != null && !items.getData().isEmpty() && items.getData().get(0).getPrice() != null)
                            ? items.getData().get(0).getPrice().getId()
                            : null;

                    if (stripePriceId == null) {
                        log.warn("No Stripe price ID found in session line items; cannot map to Pricing.");
                        return ResponseEntity.ok("ignored");
                    }

                    // Find our pricing by stripePriceId
                    Optional<Pricing> matched = pricingService.findAll().stream()
                            .filter(p -> stripePriceId.equals(p.getStripePriceId()))
                            .findFirst();

                    if (matched.isEmpty()) {
                        log.warn("No Pricing mapped to stripePriceId {}", stripePriceId);
                        return ResponseEntity.ok("ignored");
                    }

                    Pricing pricing = matched.get();

                    // Subscribe user (idempotency: SubscriptionService should prevent duplicates)
                    try {
                        SubscriptionDto subscription = subscriptionService.subscribe(user.getId(), pricing.getId());
                        log.info("Subscription created for user {} to pricing {} until {}", user.getEmail(), subscription.getPricingName(), subscription.getEndDate());
                    } catch (Exception e) {
                        log.error("Failed to create subscription after Stripe checkout for user {}: {}", user.getEmail(), e.getMessage());
                    }
                }
            }

            return ResponseEntity.ok("");
        } catch (SignatureVerificationException e) {
            log.error("Stripe signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("invalid signature");
        } catch (Exception e) {
            log.error("Stripe webhook processing failed: {}", e.getMessage());
            return ResponseEntity.ok("error");
        }
    }
}
