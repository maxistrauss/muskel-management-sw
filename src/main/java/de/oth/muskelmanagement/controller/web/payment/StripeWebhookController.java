package de.oth.muskelmanagement.controller.web.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.Stripe;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
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
import java.util.ArrayList;
import java.util.List;

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
                log.debug("Processing checkout.session.completed event");
                
                try {
                    // Parse the raw JSON payload directly
                    JsonObject root = JsonParser.parseString(payload).getAsJsonObject();
                    JsonObject data = root.getAsJsonObject("data").getAsJsonObject("object");
                    
                    String customerEmail = null;
                    if (data.has("customer_details") && data.get("customer_details").isJsonObject()) {
                        JsonObject customerDetails = data.get("customer_details").getAsJsonObject();
                        if (customerDetails.has("email") && !customerDetails.get("email").isJsonNull()) {
                            customerEmail = customerDetails.get("email").getAsString();
                        }
                    }
                    
                    log.debug("Customer email from session: {}", customerEmail);
                    
                    if (customerEmail == null || customerEmail.isBlank()) {
                        log.warn("checkout.session.completed without customer email; cannot match user.");
                        return ResponseEntity.ok("ignored");
                    }
                    
                    User user = userService.findByEmail(customerEmail);
                    log.debug("User found for email {}: {}", customerEmail, user != null);
                    
                    if (user == null) {
                        log.warn("No local user found for email {}", customerEmail);
                        return ResponseEntity.ok("ignored");
                    }
                    
                    // Get subscription ID from the event
                    String subscriptionId = null;
                    if (data.has("subscription") && !data.get("subscription").isJsonNull()) {
                        subscriptionId = data.get("subscription").getAsString();
                    }
                    
                    log.debug("Stripe subscription ID: {}", subscriptionId);
                    
                    if (subscriptionId == null || subscriptionId.isBlank()) {
                        log.warn("No subscription ID in checkout session.");
                        return ResponseEntity.ok("ignored");
                    }
                    
                    // Initialize Stripe SDK for follow-up API calls
                    Stripe.apiKey = stripeSecretKey;
                    
                    // Retrieve the subscription to get line items and price IDs
                    com.stripe.model.Subscription subscription = com.stripe.model.Subscription.retrieve(subscriptionId);
                    log.debug("Retrieved subscription: {}", subscription.getId());
                    
                    // Get the first line item to find the price ID
                    String stripePriceId = null;
                    if (subscription.getItems() != null && subscription.getItems().getData() != null && !subscription.getItems().getData().isEmpty()) {
                        com.stripe.model.SubscriptionItem item = subscription.getItems().getData().get(0);
                        stripePriceId = item.getPrice().getId();
                    }
                    
                    log.debug("Stripe Price ID from subscription: {}", stripePriceId);
                    
                    if (stripePriceId == null) {
                        log.warn("No Stripe price ID found in subscription; cannot map to Pricing.");
                        return ResponseEntity.ok("ignored");
                    }
                    
                    // Find our pricing by stripePriceId
                    final String finalStripePriceId = stripePriceId;
                    log.debug("Searching for Pricing with stripePriceId: {}", finalStripePriceId);
                    Optional<Pricing> matched = pricingService.findAll().stream()
                            .peek(p -> log.debug("  - Pricing: {} (stripePriceId: {})", p.getName(), p.getStripePriceId()))
                            .filter(p -> finalStripePriceId.equals(p.getStripePriceId()))
                            .findFirst();
                    
                    if (matched.isEmpty()) {
                        log.warn("No Pricing mapped to stripePriceId {}", stripePriceId);
                        // Fallback: try to map by Stripe Price nickname or Product name
                        try {
                            Price price = Price.retrieve(stripePriceId);
                            String nickname = price.getNickname();
                            String productId = price.getProduct();
                            String productName = null;
                            if (productId != null) {
                                try {
                                    Product product = Product.retrieve(productId);
                                    productName = product.getName();
                                } catch (Exception ignored) {
                                }
                            }

                            log.debug("Stripe price nickname: {}, product: {} ({})", nickname, productId, productName);

                            List<String> candidates = new ArrayList<>();
                            if (nickname != null && !nickname.isBlank()) candidates.add(nickname);
                            if (productName != null && !productName.isBlank()) candidates.add(productName);

                            // German product names -> internal plan names mapping
                            List<String> mappedCandidates = new ArrayList<>(candidates);
                            for (String c : candidates) {
                                String lc = c.toLowerCase().trim();
                                switch (lc) {
                                    case "standard abo":
                                    case "standard-abo":
                                    case "standard":
                                        mappedCandidates.add("Basic");
                                        break;
                                    case "premium abo":
                                    case "premium-abo":
                                        mappedCandidates.add("Premium");
                                        break;
                                    case "student abo":
                                    case "student-abo":
                                    case "student":
                                        mappedCandidates.add("Student");
                                        break;
                                }
                            }

                            Optional<Pricing> byName = pricingService.findAll().stream()
                                    .filter(p -> mappedCandidates.stream().anyMatch(c -> c.equalsIgnoreCase(p.getName())))
                                    .findFirst();

                            if (byName.isPresent()) {
                                Pricing foundByName = byName.get();
                                log.info("Matched Pricing by name: {}. Persisting stripePriceId {} for future events.", foundByName.getName(), stripePriceId);
                                try {
                                    pricingService.updateStripePriceId(foundByName.getId(), stripePriceId);
                                } catch (Exception persistEx) {
                                    log.warn("Failed to persist stripePriceId for pricing {}: {}", foundByName.getId(), persistEx.getMessage());
                                }
                                matched = Optional.of(foundByName);
                            } else {
                                return ResponseEntity.ok("ignored");
                            }
                        } catch (Exception ex) {
                            log.warn("Failed fallback mapping via Stripe Price/Product for {}: {}", stripePriceId, ex.getMessage());
                            return ResponseEntity.ok("ignored");
                        }
                    }
                    
                    Pricing pricing = matched.get();
                    log.info("Matched Pricing: {}", pricing.getName());
                    
                    // Subscribe user (idempotency: SubscriptionService should prevent duplicates)
                    try {
                        SubscriptionDto subscriptionDto = subscriptionService.subscribe(user.getId(), pricing.getId());
                        // Mark payment as PAID for Stripe-based subscriptions (using Stripe subscription id as reference)
                        try {
                            subscriptionService.markAsPaid(subscriptionDto.getId(), "STRIPE-" + subscriptionId);
                        } catch (Exception markPaidEx) {
                            log.warn("Could not mark subscription {} as PAID via Stripe: {}", subscriptionDto.getId(), markPaidEx.getMessage());
                        }
                        log.info("✅ Subscription created/confirmed for user {} to pricing {} until {}", user.getEmail(), subscriptionDto.getPricingName(), subscriptionDto.getEndDate());
                    } catch (Exception e) {
                        log.error("❌ Failed to create subscription after Stripe checkout for user {}: {}", user.getEmail(), e.getMessage(), e);
                    }
                } catch (Exception e) {
                    log.error("❌ Error processing checkout.session.completed: {}", e.getMessage(), e);
                    return ResponseEntity.ok("error");
                }
            }

            return ResponseEntity.ok("");
        } catch (SignatureVerificationException e) {
            log.error("Stripe signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("invalid signature");
        } catch (Exception e) {
            log.error("Stripe webhook processing failed: {}", e.getMessage(), e);
            return ResponseEntity.ok("error");
        }
    }
}
