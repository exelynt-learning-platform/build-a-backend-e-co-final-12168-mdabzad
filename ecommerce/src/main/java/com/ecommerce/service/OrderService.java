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
                .status(OrderStatus.PENDING)
                .build();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // Re-fetch product to ensure we have the latest stock and version
            product = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + product.getName()));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Update stock - Optimistic locking via @Version will handle race conditions
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

        // Payment Processing (Simulated Integration)
        try {
            String paymentIntentId = paymentService.createPaymentIntent(total, "usd");
            order.setPaymentIntentId(paymentIntentId);
            order.setStatus(OrderStatus.PAID); 
        } catch (Exception e) {
            // Throwing RuntimeException inside @Transactional will roll back stock updates
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
