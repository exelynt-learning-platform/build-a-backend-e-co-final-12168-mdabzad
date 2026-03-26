package com.ecommerce.controller;

import com.ecommerce.model.CartItem;
import com.ecommerce.security.SecurityUtils;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public List<CartItem> getCartItems() {
        return cartService.getCartItems(securityUtils.getCurrentUser());
    }

    @PostMapping("/add")
    public CartItem addToCart(@RequestParam Long productId, @RequestParam Integer quantity) {
        return cartService.addToCart(productId, quantity, securityUtils.getCurrentUser());
    }

    @PutMapping("/update/{cartItemId}")
    public CartItem updateQuantity(@PathVariable Long cartItemId, @RequestParam Integer quantity) {
        return cartService.updateQuantity(cartItemId, quantity, securityUtils.getCurrentUser());
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId, securityUtils.getCurrentUser());
        return ResponseEntity.ok("Item removed from cart");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        cartService.clearCart(securityUtils.getCurrentUser());
        return ResponseEntity.ok("Cart cleared");
    }
}
