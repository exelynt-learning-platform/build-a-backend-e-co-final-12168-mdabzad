package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
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
    private CartItemRepository cartItemRepository;

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
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArguments()[0]);

        CartItem result = cartService.addToCart(1L, 2, user);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        assertEquals("Laptop", result.getProduct().getName());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddToCart_ExistingItem() {
        CartItem existingItem = CartItem.builder().user(user).product(product).quantity(1).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArguments()[0]);

        CartItem result = cartService.addToCart(1L, 2, user);

        assertEquals(3, result.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void testAddToCart_InvalidQuantity() {
        assertThrows(ValidationException.class, () -> cartService.addToCart(1L, 0, user));
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void testAddToCart_ProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(1L, 2, user));
    }
}
