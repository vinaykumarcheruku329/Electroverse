package com.electroverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ShoppingResponses {

    private ShoppingResponses() {
    }

    public record CartItem(
            Long id,
            Long productId,
            String name,
            String slug,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal,
            int stock,
            boolean available
    ) {
    }

    public record Cart(
            Long cartId,
            List<CartItem> items,
            int itemCount,
            BigDecimal subtotal
    ) {
    }

    public record Address(
            Long id,
            String fullName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country,
            boolean defaultAddress
    ) {
    }

    public record OrderItem(
            Long id,
            Long productId,
            String productName,
            String productSlug,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
    }

    public record OrderStatusHistory(
            String status,
            String note,
            LocalDateTime changedAt
    ) {
    }

    public record Order(
            Long id,
            String orderNumber,
            String status,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal tax,
            BigDecimal total,
            String paymentMethod,
            String shippingFullName,
            String shippingPhone,
            String shippingLine1,
            String shippingLine2,
            String shippingCity,
            String shippingState,
            String shippingPostalCode,
            String shippingCountry,
            List<OrderItem> items,
            List<OrderStatusHistory> statusHistory,
            boolean cancellable,
            LocalDateTime createdAt
    ) {
    }

    public record Review(
            Long id,
            Long productId,
            Long userId,
            String userName,
            int rating,
            String title,
            String comment,
            String status,
            LocalDateTime createdAt
    ) {
    }

    public record PriceAlert(
            Long id,
            Long productId,
            String productName,
            String productSlug,
            BigDecimal targetPrice,
            boolean active,
            boolean triggered,
            LocalDateTime triggeredAt,
            BigDecimal lastObservedPrice,
            LocalDateTime createdAt
    ) {
    }
}
