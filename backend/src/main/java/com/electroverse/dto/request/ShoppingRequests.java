package com.electroverse.dto.request;

import com.electroverse.entity.enums.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ShoppingRequests {

    private ShoppingRequests() {
    }

    public record CartItem(
            @NotNull(message = "Product id is required")
            Long productId,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 99, message = "Quantity is too large")
            Integer quantity
    ) {
    }

    public record CartUpdate(
            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 99, message = "Quantity is too large")
            Integer quantity
    ) {
    }

    public record Address(
            @NotBlank(message = "Full name is required")
            @Size(max = 120)
            String fullName,

            @NotBlank(message = "Phone is required")
            @Size(max = 30)
            String phone,

            @NotBlank(message = "Address line 1 is required")
            @Size(max = 200)
            String line1,

            @Size(max = 200)
            String line2,

            @NotBlank(message = "City is required")
            @Size(max = 100)
            String city,

            @NotBlank(message = "State is required")
            @Size(max = 100)
            String state,

            @NotBlank(message = "Postal code is required")
            @Size(max = 20)
            String postalCode,

            @NotBlank(message = "Country is required")
            @Size(max = 100)
            String country,

            Boolean defaultAddress
    ) {
    }

    public record Checkout(
            @NotNull(message = "Address is required")
            Long addressId,

            @NotBlank(message = "Payment method is required")
            String paymentMethod,

            @Size(max = 300)
            String notes
    ) {
    }

    public record Review(
            @NotNull(message = "Rating is required")
            @Min(value = 1, message = "Rating must be between 1 and 5")
            @Max(value = 5, message = "Rating must be between 1 and 5")
            Integer rating,

            @Size(max = 150)
            String title,

            @Size(max = 2000)
            String comment
    ) {
    }

    public record PriceAlert(
            @NotNull(message = "Product id is required")
            Long productId,

            @NotNull(message = "Target price is required")
            @DecimalMin(value = "0.0", inclusive = false, message = "Target price must be positive")
            BigDecimal targetPrice
    ) {
    }

    public record OrderStatusUpdate(
            @NotNull(message = "Status is required")
            OrderStatus status,

            @Size(max = 300)
            String note
    ) {
    }

    public record UserStatusUpdate(
            @NotNull(message = "Active flag is required")
            Boolean active
    ) {
    }
}
