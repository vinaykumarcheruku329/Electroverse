package com.electroverse.service;

import com.electroverse.entity.Cart;
import com.electroverse.entity.CartItem;
import com.electroverse.entity.Product;
import com.electroverse.entity.User;
import com.electroverse.exception.BadRequestException;
import com.electroverse.repository.CartItemRepository;
import com.electroverse.repository.CartRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private SecurityUtils securityUtils;

    private CartService service;
    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        service = new CartService(cartRepository, cartItemRepository, productRepository, securityUtils);

        user = new User();
        user.setId(1L);
        user.setEmail("customer@test.local");

        cart = new Cart();
        cart.setId(5L);
        cart.setUser(user);

        product = new Product();
        product.setId(10L);
        product.setName("Test Laptop");
        product.setSlug("test-laptop");
        product.setPrice(new BigDecimal("1000.00"));
        product.setStock(3);
        product.setActive(true);

        lenient().when(securityUtils.getCurrentUser()).thenReturn(user);
        lenient().when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(productRepository.findById(10L)).thenReturn(Optional.of(product));
    }

    @Test
    void rejectsAddWhenQuantityExceedsStock() {
        when(cartItemRepository.findByCartIdAndProductId(5L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addItem(10L, 5))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("available in stock");
    }

    @Test
    void mergesQuantityWithExistingItem() {
        CartItem existing = new CartItem();
        existing.setId(7L);
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQuantity(2);
        when(cartItemRepository.findByCartIdAndProductId(5L, 10L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByCartIdOrderByIdAsc(5L)).thenReturn(List.of(existing));

        service.addItem(10L, 1);

        assertThat(existing.getQuantity()).isEqualTo(3);
    }

    @Test
    void updateRejectsQuantityAboveStock() {
        CartItem existing = new CartItem();
        existing.setId(7L);
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQuantity(1);
        when(cartItemRepository.findByIdAndCartUserId(7L, 1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateItem(7L, 10))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateFailsForItemOwnedByAnotherUser() {
        when(cartItemRepository.findByIdAndCartUserId(7L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateItem(7L, 1))
                .isInstanceOf(com.electroverse.exception.ResourceNotFoundException.class);
    }

    @Test
    void computesSubtotalAndCount() {
        CartItem item = new CartItem();
        item.setId(7L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);
        when(cartItemRepository.findByCartIdOrderByIdAsc(5L)).thenReturn(List.of(item));

        var response = service.getCart();

        assertThat(response.itemCount()).isEqualTo(2);
        assertThat(response.subtotal()).isEqualByComparingTo("2000.00");
    }
}
