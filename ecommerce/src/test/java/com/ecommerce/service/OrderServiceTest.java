package com.ecommerce.service;

import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private List<CartItem> cartItems;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder().id(1L).name("Laptop").price(new BigDecimal("1000")).stock(10).build();
        
        cartItems = new ArrayList<>();
        cartItems.add(CartItem.builder().user(user).product(product).quantity(2).build());
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        when(cartService.getCartItems(user)).thenReturn(cartItems);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        when(paymentService.createPaymentIntent(any(BigDecimal.class), anyString())).thenReturn("pi_demo_123");

        Order result = orderService.createOrder(user, "123 Street");

        assertNotNull(result);
        assertEquals(new BigDecimal("2000"), result.getTotalPrice());
        assertEquals("PAID", result.getStatus());
        assertEquals(8, product.getStock());
        verify(cartService, times(1)).clearCart(user);
    }

    @Test
    void testCreateOrder_InsufficientStock() {
        product.setStock(1);
        when(cartService.getCartItems(user)).thenReturn(cartItems);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(user, "123 Street"));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
