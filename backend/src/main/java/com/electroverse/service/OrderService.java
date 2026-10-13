package com.electroverse.service;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.entity.Address;
import com.electroverse.entity.Cart;
import com.electroverse.entity.CartItem;
import com.electroverse.entity.Order;
import com.electroverse.entity.OrderItem;
import com.electroverse.entity.OrderStatusHistory;
import com.electroverse.entity.Product;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.OrderStatus;
import com.electroverse.entity.enums.PaymentMethod;
import com.electroverse.exception.BadRequestException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.AddressRepository;
import com.electroverse.repository.CartItemRepository;
import com.electroverse.repository.CartRepository;
import com.electroverse.repository.OrderRepository;
import com.electroverse.repository.OrderStatusHistoryRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("5000.00");
    public static final BigDecimal SHIPPING_FEE = new BigDecimal("99.00");
    public static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
        TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final SecurityUtils securityUtils;

    public OrderService(OrderRepository orderRepository,
                        OrderStatusHistoryRepository historyRepository,
                        ProductRepository productRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        AddressRepository addressRepository,
                        SecurityUtils securityUtils) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public ShoppingResponses.Order checkout(ShoppingRequests.Checkout request) {
        User user = securityUtils.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(request.addressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + request.addressId()));
        PaymentMethod paymentMethod = parsePaymentMethod(request.paymentMethod());

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        if (items.isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        List<CartItem> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(ci -> ci.getProduct().getId()));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingFullName(address.getFullName());
        order.setShippingPhone(address.getPhone());
        order.setShippingLine1(address.getLine1());
        order.setShippingLine2(address.getLine2());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingCountry(address.getCountry());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : sorted) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new BadRequestException("A product in your cart is no longer available"));
            if (!product.isActive()) {
                throw new BadRequestException("Product not available: " + product.getName());
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName()
                        + ". Available: " + product.getStock());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductSlug(product.getSlug());
            orderItem.setImageUrl(product.getPrimaryImageUrl());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setLineTotal(lineTotal);
            order.getItems().add(orderItem);
        }

        BigDecimal shippingFee = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO : SHIPPING_FEE;
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(shippingFee).add(tax);

        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTax(tax);
        order.setTotal(total);

        OrderStatusHistory historyEntry = new OrderStatusHistory();
        historyEntry.setOrder(order);
        historyEntry.setStatus(OrderStatus.PENDING);
        historyEntry.setNote(request.notes() != null ? request.notes() : "Order placed");
        historyEntry.setChangedAt(LocalDateTime.now());
        order.getStatusHistory().add(historyEntry);

        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return DtoMapper.toOrder(saved);
    }

    @Transactional(readOnly = true)
    public CatalogResponses.Paged<ShoppingResponses.Order> listMine(Pageable pageable) {
        User user = securityUtils.getCurrentUser();
        Page<Order> page = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return DtoMapper.toPaged(page, DtoMapper::toOrder);
    }

    @Transactional(readOnly = true)
    public ShoppingResponses.Order getMine(Long id) {
        User user = securityUtils.getCurrentUser();
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return DtoMapper.toOrder(order);
    }

    @Transactional
    public ShoppingResponses.Order cancel(Long id) {
        User user = securityUtils.getCurrentUser();
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Orders can only be cancelled while PENDING or CONFIRMED");
        }
        restoreStock(order);
        transition(order, OrderStatus.CANCELLED, "Order cancelled by customer");
        return DtoMapper.toOrder(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public CatalogResponses.Paged<ShoppingResponses.Order> adminList(Pageable pageable) {
        Page<Order> page = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        return DtoMapper.toPaged(page, DtoMapper::toOrder);
    }

    @Transactional(readOnly = true)
    public ShoppingResponses.Order adminGet(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return DtoMapper.toOrder(order);
    }

    @Transactional
    public ShoppingResponses.Order adminUpdateStatus(Long id, ShoppingRequests.OrderStatusUpdate request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        OrderStatus current = order.getStatus();
        OrderStatus target = request.status();
        if (current == target) {
            return DtoMapper.toOrder(order);
        }
        Set<OrderStatus> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(target)) {
            throw new BadRequestException("Cannot change order status from " + current + " to " + target);
        }
        if (target == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        transition(order, target, request.note() != null ? request.note() : "Status updated by administrator");
        return DtoMapper.toOrder(orderRepository.save(order));
    }

    private void restoreStock(Order order) {
        List<OrderItem> items = new ArrayList<>(order.getItems());
        items.sort(Comparator.comparing(oi -> oi.getProduct() != null ? oi.getProduct().getId() : Long.MAX_VALUE));
        for (OrderItem item : items) {
            if (item.getProduct() == null) {
                continue;
            }
            productRepository.findByIdForUpdate(item.getProduct().getId()).ifPresent(product -> {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            });
        }
    }

    private void transition(Order order, OrderStatus status, String note) {
        order.setStatus(status);
        OrderStatusHistory entry = new OrderStatusHistory();
        entry.setOrder(order);
        entry.setStatus(status);
        entry.setNote(note);
        entry.setChangedAt(LocalDateTime.now());
        order.getStatusHistory().add(entry);
    }

    private PaymentMethod parsePaymentMethod(String value) {
        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported payment method. Use CASH_ON_DELIVERY or SIMULATED_CARD");
        }
    }

    private String generateOrderNumber() {
        String candidate;
        do {
            candidate = "EV-" + LocalDateTime.now().format(ORDER_DATE) + "-"
                    + ThreadLocalRandom.current().nextInt(100000, 999999);
        } while (orderRepository.findByOrderNumber(candidate).isPresent());
        return candidate;
    }
}
