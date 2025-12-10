package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.SubscriptionDto;
import de.oth.muskelmanagement.service.PayPalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Base64;

@Service
public class PayPalServiceImpl implements PayPalService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalServiceImpl.class);
    
    @Value("${paypal.client-id}")
    private String clientId;
    
    @Value("${paypal.client-secret}")
    private String clientSecret;
    
    @Value("${paypal.mode}")
    private String mode;
    
    @Value("${paypal.return-url}")
    private String returnUrl;
    
    @Value("${paypal.cancel-url}")
    private String cancelUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String baseUrl;
    
    private void initializeBaseUrl() {
        if (baseUrl == null) {
            baseUrl = "sandbox".equalsIgnoreCase(mode) 
                ? "https://api-m.sandbox.paypal.com" 
                : "https://api-m.paypal.com";
        }
    }

    @Override
    public String getAccessToken() {
        try {
            initializeBaseUrl();
            
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/v1/oauth2/token",
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String accessToken = jsonResponse.get("access_token").asText();
                logger.info("Successfully obtained PayPal access token");
                return accessToken;
            }
        } catch (Exception e) {
            logger.error("Error getting PayPal access token", e);
        }
        return null;
    }

    @Override
    public String createOrder(SubscriptionDto subscriptionDto) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                throw new RuntimeException("Failed to get PayPal access token");
            }
            
            initializeBaseUrl();
            
            String requestBody = buildOrderRequest(subscriptionDto);
            logger.info("Creating PayPal order with payload: {}", requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Prefer", "return=representation");
            
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders",
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String orderId = jsonResponse.get("id").asText();
                logger.info("Created PayPal order: {}", orderId);
                return orderId;
            }
        } catch (Exception e) {
            logger.error("Error creating PayPal order", e);
        }
        return null;
    }

    @Override
    public boolean captureOrder(String orderId) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                throw new RuntimeException("Failed to get PayPal access token");
            }
            
            initializeBaseUrl();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Prefer", "return=representation");
            
            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String status = jsonResponse.get("status").asText();
                
                if ("COMPLETED".equalsIgnoreCase(status)) {
                    logger.info("PayPal order {} captured successfully", orderId);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Error capturing PayPal order", e);
        }
        return false;
    }
    
    private String buildOrderRequest(SubscriptionDto subscription) throws Exception {
        ObjectNode order = objectMapper.createObjectNode();
        order.put("intent", "CAPTURE");
        
        // Purchase units
        ArrayNode purchaseUnits = objectMapper.createArrayNode();
        ObjectNode unit = objectMapper.createObjectNode();
        unit.put("reference_id", "subscription-" + subscription.getId());
        
        // Amount
        ObjectNode amount = objectMapper.createObjectNode();
        amount.put("currency_code", "EUR");
        amount.put("value", subscription.getPricingPrice().toString());
        
        // Breakdown
        ObjectNode breakdown = objectMapper.createObjectNode();
        ObjectNode itemTotal = objectMapper.createObjectNode();
        itemTotal.put("currency_code", "EUR");
        itemTotal.put("value", subscription.getPricingPrice().toString());
        breakdown.set("item_total", itemTotal);
        amount.set("breakdown", breakdown);
        unit.set("amount", amount);
        
        // Items
        ArrayNode items = objectMapper.createArrayNode();
        ObjectNode item = objectMapper.createObjectNode();
        item.put("name", subscription.getPricingName() + " - " + subscription.getPricingDuration() + " month(s)");
        item.put("description", subscription.getPricingDescription() != null ? subscription.getPricingDescription() : "Gym Membership");
        
        ObjectNode itemAmount = objectMapper.createObjectNode();
        itemAmount.put("currency_code", "EUR");
        itemAmount.put("value", subscription.getPricingPrice().toString());
        item.set("unit_amount", itemAmount);
        
        item.put("quantity", "1");
        item.put("category", "DIGITAL_SERVICE");
        items.add(item);
        unit.set("items", items);
        
        purchaseUnits.add(unit);
        order.set("purchase_units", purchaseUnits);
        
        // Payment source
        ObjectNode paymentSource = objectMapper.createObjectNode();
        ObjectNode paypal = objectMapper.createObjectNode();
        ObjectNode experienceContext = objectMapper.createObjectNode();
        experienceContext.put("return_url", returnUrl);
        experienceContext.put("cancel_url", cancelUrl);
        experienceContext.put("user_action", "PAY_NOW");
        paypal.set("experience_context", experienceContext);
        paymentSource.set("paypal", paypal);
        order.set("payment_source", paymentSource);
        
        return objectMapper.writeValueAsString(order);
    }
}

