package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.model.entity.Subscription;
import de.oth.muskelmanagement.model.entity.Pricing;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import de.oth.muskelmanagement.repository.SubscriptionRepository;
import de.oth.muskelmanagement.repository.PricingRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PricingRepository pricingRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, UserRepository userRepository,
            PricingRepository pricingRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.pricingRepository = pricingRepository;
    }

    @Override
    @Transactional
    public SubscriptionDto subscribe(Long userId, Long pricingId) {
        logger.info("Creating subscription for user {} with pricing {}", userId, pricingId);

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if pricing exists and is active
        Pricing pricing = pricingRepository.findById(pricingId)
                .orElseThrow(() -> new RuntimeException("Pricing not found with id: " + pricingId));

        if (!pricing.isActive()) {
            throw new RuntimeException("Cannot subscribe to inactive pricing: " + pricing.getName());
        }

        // Check if user already has an active subscription
        if (subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new RuntimeException("User already has an active subscription");
        }

        // Create new subscription
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(pricing.getDurationMonths());

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPricing(pricing);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAutoRenew(false);

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        logger.info("Subscription created successfully with id: {}", savedSubscription.getId());

        return convertToDto(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionDto> getActiveSubscription(Long userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDto> getUserSubscriptionHistory(Long userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        logger.info("Cancelling subscription with id: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));

        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new RuntimeException("Cannot cancel expired subscription");
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new RuntimeException("Subscription is already cancelled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        logger.info("Subscription {} cancelled successfully", subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSubscribe(Long userId) {
        return !subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void updateExpiredSubscriptions() {
        logger.info("Updating expired subscriptions");

        LocalDate today = LocalDate.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findByStatusAndEndDateBefore(
                SubscriptionStatus.ACTIVE, today);

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            logger.info("Subscription {} marked as expired", subscription.getId());
        }

        logger.info("Updated {} expired subscriptions", expiredSubscriptions.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + id));
        return convertToDto(subscription);
    }

    @Override
    @Transactional
    public void markAsPaid(Long subscriptionId, String paypalOrderId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));
        
        subscription.setPaypalOrderId(paypalOrderId);
        subscription.setPaymentStatus("PAID");
        subscriptionRepository.save(subscription);
        
        logger.info("Subscription {} marked as paid with PayPal order {}", subscriptionId, paypalOrderId);
    }

    @Override
    @Transactional
    public void updatePayPalOrderId(Long subscriptionId, String paypalOrderId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));
        
        subscription.setPaypalOrderId(paypalOrderId);
        subscription.setPaymentStatus("PENDING");
        subscriptionRepository.save(subscription);
        
        logger.info("Subscription {} updated with PayPal order {}", subscriptionId, paypalOrderId);
    }

    @Override
    @Transactional
    public void markAsPaidByUserId(Long userId, String paypalOrderId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found for user: " + userId));
        
        subscription.setPaypalOrderId(paypalOrderId);
        subscription.setPaymentStatus("PAID");
        subscriptionRepository.save(subscription);
        
        logger.info("Subscription {} for user {} marked as paid with PayPal order {}", 
                subscription.getId(), userId, paypalOrderId);
    }

    private SubscriptionDto convertToDto(Subscription subscription) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(subscription.getId());
        dto.setUserId(subscription.getUser().getId());
        dto.setUserEmail(subscription.getUser().getEmail());
        dto.setUserName(subscription.getUser().getFirstName() + " " + subscription.getUser().getLastName());
        dto.setPricingId(subscription.getPricing().getId());
        dto.setPricingName(subscription.getPricing().getName());
        dto.setPricingPrice(subscription.getPricing().getPrice());
        dto.setPricingDuration(subscription.getPricing().getDurationMonths());
        dto.setPricingDescription(subscription.getPricing().getDescription());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());
        dto.setStatus(subscription.getStatus());
        dto.setAutoRenew(subscription.getAutoRenew());
        dto.setCreatedAt(subscription.getCreatedAt());
        dto.setUpdatedAt(subscription.getUpdatedAt());
        dto.setPaypalOrderId(subscription.getPaypalOrderId());
        dto.setPaymentStatus(subscription.getPaymentStatus());
        return dto;
    }
}
