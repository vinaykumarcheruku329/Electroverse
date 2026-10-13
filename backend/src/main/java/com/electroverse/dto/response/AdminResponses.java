package com.electroverse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminResponses {

    private AdminResponses() {
    }

    public record Dashboard(
            long totalUsers,
            long totalCustomers,
            long totalProducts,
            long activeProducts,
            long totalOrders,
            long pendingOrders,
            long deliveredOrders,
            long cancelledOrders,
            BigDecimal totalRevenue,
            long lowStockProducts,
            long totalReviews,
            long pendingReviews,
            long triggeredAlerts,
            long finderSessions,
            List<CountItem> ordersByStatus,
            List<CountItem> productsByCategory,
            List<ShoppingResponses.Order> recentOrders
    ) {
    }

    public record CountItem(
            String label,
            long value
    ) {
    }

    public record InventoryRow(
            Long productId,
            String name,
            String sku,
            String categoryName,
            int stock,
            BigDecimal price,
            boolean active,
            String stockStatus
    ) {
    }

    public record LowStockAlert(
            Long productId,
            String name,
            int stock,
            int threshold
    ) {
    }

    public record ReviewModeration(
            Long id,
            Long productId,
            String productName,
            String userName,
            int rating,
            String title,
            String comment,
            String status,
            LocalDateTime createdAt
    ) {
    }
}
