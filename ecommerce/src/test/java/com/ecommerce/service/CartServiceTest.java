package com.ecommerce.service;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder().id(1L).name("Laptop").price(new BigDecimal("1000")).stock(10).build();
    }

    @Test
    void testAddToCart_NewItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProductId(user, 1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArguments()[0]);

        CartItem result = cartService.addToCart(1L, 2, user);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        assertEquals("Laptop", result.getProduct().getName());
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddToCart_ExistingItem() {
        CartItem existingItem = CartItem.builder().user(user).product(product).quantity(1).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProductId(user, 1L)).thenReturn(Optional.of(existingItem));
        when(cartRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArguments()[0]);

        CartItem result = cartService.addToCart(1L, 2, user);

        assertEquals(3, result.getQuantity());
        verify(cartRepository, times(1)).save(existingItem);
    }
}
