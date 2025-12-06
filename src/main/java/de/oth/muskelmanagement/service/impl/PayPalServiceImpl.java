package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.service.PayPalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PayPalServiceImpl implements PayPalService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalServiceImpl.class);
    
    @Override
    public String getAccessToken() {
        // Not needed for redirect flow
        return null;
    }

    @Override
    public String createOrder(SubscriptionDto subscriptionDto) {
        // Generate a unique order ID (in real scenario, this would be created via PayPal API)
        String orderId = UUID.randomUUID().toString();
        logger.info("Created PayPal order: {} for subscription: {}", orderId, subscriptionDto.getId());
        return orderId;
    }

    @Override
    public boolean captureOrder(String orderId) {
        // In redirect flow, PayPal handles capture
        // Here we just confirm it
        logger.info("PayPal order {} completed", orderId);
        return true;
    }
}
