package com.ecommerce.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment Service (Demo Implementation)
 * This service simulates a real payment gateway integration.
 * In a production environment, this would call Stripe or PayPal APIs.
 */
@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        // In a real integration, we would initialize the Stripe client here.
        logger.info("Initializing Payment Service with demo key: {}", 
            stripeApiKey.substring(0, 7) + "...");
    }

    /**
     * Simulates creating a Payment Intent/Session.
     * Returns a mock transaction ID.
     */
    public String createPaymentIntent(BigDecimal amount, String currency) throws Exception {
        logger.info("Processing demo payment of {} {}", amount, currency);
        
        // Simulate a small network delay for "realism"
        Thread.sleep(500); 
        
        // Generate a mock Stripe-like payment intent ID
        return "pi_demo_" + UUID.randomUUID().toString().substring(0, 24);
    }
}
