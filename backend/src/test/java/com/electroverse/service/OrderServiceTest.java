package com.electroverse.service;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.entity.Address;
import com.electroverse.entity.Cart;
import com.electroverse.entity.CartItem;
import com.electroverse.entity.Order;
import com.electroverse.entity.OrderItem;
import com.electroverse.entity.Product;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.OrderStatus;
import com.electroverse.entity.enums.PaymentMethod;
import com.electroverse.exception.BadRequestException;
import com.electroverse.repository.AddressRepository;
import com.electroverse.repository.CartItemRepository;
import com.electroverse.repository.CartRepository;
import com.electroverse.repository.OrderRepository;
import com.electroverse.repository.OrderStatusHistoryRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderStatusHistoryRepository historyRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private SecurityUtils securityUtils;

    private OrderService service;
    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, historyRepository, productRepository,
                cartRepository, cartItemRepository, addressRepository, securityUtils);

        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Test Laptop");
        product.setSlug("test-laptop");
        product.setPrice(new BigDecimal("3000.00"));
        product.setStock(10);
        product.setActive(true);
        product.setCurrency("INR");

        cart = new Cart();
        cart.setId(5L);
        cart.setUser(user);

        lenient().when(securityUtils.getCurrentUser()).thenReturn(user);
        lenient().when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) {
                o.setId(100L);
            }
            return o;
        });
        lenient().when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Address address() {
        Address address = new Address();
        address.setId(2L);
        address.setUser(user);
        address.setFullName("Test Customer");
        address.setPhone("1234567890");
        address.setLine1("1 Test Street");
        address.setCity("Testville");
        address.setState("TS");
        address.setPostalCode("12345");
        address.setCountry("Testland");
        return address;
    }

    private CartItem cartItem(int quantity) {
        CartItem item = new CartItem();
        item.setId(7L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void checkoutComputesServerSideTotalsAndDecrementsStock() {
        when(addressRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(address()));
        when(cartItemRepository.findByCartIdOrderByIdAsc(5L)).thenReturn(List.of(cartItem(2)));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));

        ShoppingResponses.Order order = service.checkout(
                new ShoppingRequests.Checkout(2L, "CASH_ON_DELIVERY", null));

        assertThat(order.subtotal()).isEqualByComparingTo("6000.00");
        assertThat(order.shippingFee()).isEqualByComparingTo("0.00");
        assertThat(order.tax()).isEqualByComparingTo("480.00");
        assertThat(order.total()).isEqualByComparingTo("6480.00");
        assertThat(order.status()).isEqualTo("PENDING");
        assertThat(product.getStock()).isEqualTo(8);
    }

    @Test
    void checkoutAppliesShippingBelowThreshold() {
        Product cheap = new Product();
        cheap.setId(11L);
        cheap.setName("Cable");
        cheap.setSlug("cable");
        cheap.setPrice(new BigDecimal("20.00"));
        cheap.setStock(50);
        cheap.setActive(true);
        cheap.setCurrency("INR");

        CartItem item = new CartItem();
        item.setId(8L);
        item.setCart(cart);
        item.setProduct(cheap);
        item.setQuantity(1);

        when(addressRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(address()));
        when(cartItemRepository.findByCartIdOrderByIdAsc(5L)).thenReturn(List.of(item));
        when(productRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(cheap));

        ShoppingResponses.Order order = service.checkout(
                new ShoppingRequests.Checkout(2L, "SIMULATED_CARD", null));

        assertThat(order.shippingFee()).isEqualByComparingTo("99.00");
        assertThat(order.total()).isEqualByComparingTo("120.60");
    }

    @Test
    void checkoutFailsWhenStockInsufficient() {
        product.setStock(1);
        when(addressRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(address()));
        when(cartItemRepository.findByCartIdOrderByIdAsc(5L)).thenReturn(List.of(cartItem(3)));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.checkout(new ShoppingRequests.Checkout(2L, "CASH_ON_DELIVERY", null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
        assertThat(product.getStock()).isEqualTo(1);
    }

    @Test
    void checkoutRejectsAddressBelongingToAnotherUser() {
        when(addressRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkout(new ShoppingRequests.Checkout(2L, "CASH_ON_DELIVERY", null)))
                .isInstanceOf(com.electroverse.exception.ResourceNotFoundException.class);
    }

    @Test
    void cancellingRestoresStockAndSetsCancelled() {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        order.setSubtotal(new BigDecimal("2000.00"));
        order.setShippingFee(BigDecimal.ZERO);
        order.setTax(new BigDecimal("160.00"));
        order.setTotal(new BigDecimal("2160.00"));
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setLineTotal(new BigDecimal("2000.00"));
        order.setItems(new ArrayList<>(List.of(item)));

        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));

        ShoppingResponses.Order result = service.cancel(100L);

        assertThat(result.status()).isEqualTo("CANCELLED");
        assertThat(product.getStock()).isEqualTo(12);
    }

    @Test
    void cannotCancelDeliveredOrder() {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancel(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PENDING or CONFIRMED");
    }

    @Test
    void adminCannotSkipInvalidStatusTransition() {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.adminUpdateStatus(100L,
                new ShoppingRequests.OrderStatusUpdate(OrderStatus.PENDING, "revert")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot change order status");
    }
}
