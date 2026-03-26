package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.security.SecurityUtils;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping("/checkout")
    public Order checkout(@RequestParam String shippingAddress) throws Exception {
        return orderService.createOrder(securityUtils.getCurrentUser(), shippingAddress);
    }

    @GetMapping
    public List<Order> getUserOrders() {
        return orderService.getUserOrders(securityUtils.getCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
