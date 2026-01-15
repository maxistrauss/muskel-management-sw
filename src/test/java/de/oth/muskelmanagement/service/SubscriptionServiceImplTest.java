package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.model.entity.Pricing;
import de.oth.muskelmanagement.model.entity.Subscription;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.SubscriptionStatus;
import de.oth.muskelmanagement.repository.PricingRepository;
import de.oth.muskelmanagement.repository.SubscriptionRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PricingRepository pricingRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Pricing pricing;
    private Subscription activeSubscription;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        pricing = new Pricing();
        pricing.setId(1L);
        pricing.setName("Basic");
        pricing.setDurationMonths(1);
        pricing.setActive(true);

        activeSubscription = new Subscription();
        activeSubscription.setId(1L);
        activeSubscription.setUser(user);
        activeSubscription.setPricing(pricing);
        activeSubscription.setStatus(SubscriptionStatus.ACTIVE);
        activeSubscription.setStartDate(LocalDate.now().minusDays(10));
        activeSubscription.setEndDate(LocalDate.now().plusDays(20));
    }
    @Test
    void cancelSubscription_ShouldSetStatusToCancelled() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(activeSubscription));

        subscriptionService.cancelSubscription(1L);

        assertEquals(SubscriptionStatus.CANCELLED, activeSubscription.getStatus());
        verify(subscriptionRepository).save(activeSubscription);
    }

    @Test
    void updateExpiredSubscriptions_ShouldExpireOldSubscriptions() {
        Subscription expiredSub = new Subscription();
        expiredSub.setId(2L);
        expiredSub.setStatus(SubscriptionStatus.ACTIVE);
        expiredSub.setEndDate(LocalDate.now().minusDays(1)); // Yesterday

        when(subscriptionRepository.findByStatusAndEndDateBefore(eq(SubscriptionStatus.ACTIVE),
                any(LocalDate.class))).thenReturn(List.of(expiredSub));

        subscriptionService.updateExpiredSubscriptions();

        assertEquals(SubscriptionStatus.EXPIRED, expiredSub.getStatus());
        verify(subscriptionRepository).save(expiredSub);
    }


}
