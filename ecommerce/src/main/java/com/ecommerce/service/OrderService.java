package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentService paymentService;

    @Transactional
    public Order createOrder(User user, String shippingAddress) throws Exception {
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .build();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Update stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            total = total.add(product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
        }

        order.setItems(orderItems);
        order.setTotalPrice(total);

        // Payment Processing (Simulated Stripe Integration)
        try {
            String paymentIntentId = paymentService.createPaymentIntent(total, "usd");
            order.setPaymentIntentId(paymentIntentId);
            order.setStatus("PAID"); 
        } catch (Exception e) {
            // In a real app, you might set status to 'PAYMENT_FAILED' and roll back stock
            throw new RuntimeException("Payment failed: " + e.getMessage());
        }

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(user);
        return savedOrder;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUser(user);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
}
