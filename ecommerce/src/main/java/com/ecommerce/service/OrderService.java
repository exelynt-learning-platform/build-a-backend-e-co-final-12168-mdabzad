package com.ecommerce.service;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
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

    /**
     * Creates an order with pessimistic locking to prevent race conditions in inventory.
     * The transaction will roll back stock updates if payment creation fails.
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(User user, String shippingAddress) throws Exception {
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();

        // Validate and reduce stock with pessimistic locking
        for (CartItem cartItem : cartItems) {
            if (cartItem.getQuantity() <= 0) {
                throw new ValidationException("Invalid quantity for product: " + cartItem.getProduct().getName());
            }

            // Fetch with PESSIMISTIC_WRITE lock
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProduct().getName()));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Update stock immediately within the transaction
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

        // Payment Processing - Failure here will trigger @Transactional rollback of stock
        try {
            String paymentIntentId = paymentService.createPaymentIntent(total, "usd");
            order.setPaymentIntentId(paymentIntentId);
            order.setStatus(OrderStatus.PAID); 
        } catch (Exception e) {
            // Rethrowing as Exception to ensure rollback
            throw new Exception("Payment failed, stock restored: " + e.getMessage());
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
