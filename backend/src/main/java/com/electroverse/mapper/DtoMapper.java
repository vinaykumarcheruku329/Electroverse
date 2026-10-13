package com.electroverse.mapper;

import com.electroverse.dto.response.AdminResponses;
import com.electroverse.dto.response.AuthResponses;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.entity.Address;
import com.electroverse.entity.CartItem;
import com.electroverse.entity.Category;
import com.electroverse.entity.CompatibilityRule;
import com.electroverse.entity.LifecycleAssessment;
import com.electroverse.entity.Order;
import com.electroverse.entity.OrderItem;
import com.electroverse.entity.OrderStatusHistory;
import com.electroverse.entity.PriceAlert;
import com.electroverse.entity.Product;
import com.electroverse.entity.ProductImage;
import com.electroverse.entity.ProductSpec;
import com.electroverse.entity.Review;
import com.electroverse.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static AuthResponses.User toUser(User user) {
        return new AuthResponses.User(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    public static CatalogResponses.Category toCategory(Category category, long productCount) {
        return new CatalogResponses.Category(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIcon(),
                productCount
        );
    }

    public static CatalogResponses.Spec toSpec(ProductSpec spec) {
        return new CatalogResponses.Spec(
                spec.getSpecGroup(),
                spec.getSpecKey(),
                spec.getSpecValue(),
                spec.getNumericValue()
        );
    }

    public static CatalogResponses.Image toImage(ProductImage image) {
        return new CatalogResponses.Image(image.getUrl(), image.getAltText(), image.getSortOrder());
    }

    public static CatalogResponses.ProductSummary toProductSummary(Product product) {
        return new CatalogResponses.ProductSummary(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getBrand(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getCurrency(),
                product.getStock(),
                product.getRatingAverage(),
                product.getRatingCount(),
                product.getPrimaryImageUrl(),
                product.isFeatured(),
                product.getWarrantyMonths()
        );
    }

    public static CatalogResponses.ProductDetail toProductDetail(Product product) {
        List<CatalogResponses.Image> images = product.getImages().stream()
                .map(DtoMapper::toImage)
                .toList();
        List<CatalogResponses.Spec> specs = product.getSpecs().stream()
                .map(DtoMapper::toSpec)
                .toList();
        return new CatalogResponses.ProductDetail(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getBrand(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                product.getDescription(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getCurrency(),
                product.getStock(),
                product.getRatingAverage(),
                product.getRatingCount(),
                product.getWarrantyMonths(),
                product.getPrimaryImageUrl(),
                product.getReleaseDate(),
                product.isFeatured(),
                product.isActive(),
                product.getCreatedAt(),
                images,
                specs
        );
    }

    public static ShoppingResponses.Address toAddress(Address address) {
        return new ShoppingResponses.Address(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefaultAddress()
        );
    }

    public static ShoppingResponses.CartItem toCartItem(CartItem item) {
        Product product = item.getProduct();
        java.math.BigDecimal lineTotal = product.getPrice()
                .multiply(java.math.BigDecimal.valueOf(item.getQuantity()));
        return new ShoppingResponses.CartItem(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrimaryImageUrl(),
                product.getPrice(),
                item.getQuantity(),
                lineTotal,
                product.getStock(),
                product.isActive() && product.getStock() >= item.getQuantity()
        );
    }

    public static ShoppingResponses.OrderItem toOrderItem(OrderItem item) {
        return new ShoppingResponses.OrderItem(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductName(),
                item.getProductSlug(),
                item.getImageUrl(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    public static ShoppingResponses.OrderStatusHistory toStatusHistory(OrderStatusHistory history) {
        return new ShoppingResponses.OrderStatusHistory(
                history.getStatus().name(),
                history.getNote(),
                history.getChangedAt()
        );
    }

    public static ShoppingResponses.Order toOrder(Order order) {
        List<ShoppingResponses.OrderItem> items = order.getItems().stream()
                .map(DtoMapper::toOrderItem)
                .toList();
        List<ShoppingResponses.OrderStatusHistory> history = order.getStatusHistory().stream()
                .map(DtoMapper::toStatusHistory)
                .toList();
        return new ShoppingResponses.Order(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTax(),
                order.getTotal(),
                order.getPaymentMethod().name(),
                order.getShippingFullName(),
                order.getShippingPhone(),
                order.getShippingLine1(),
                order.getShippingLine2(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode(),
                order.getShippingCountry(),
                items,
                history,
                order.getStatus() == com.electroverse.entity.enums.OrderStatus.PENDING
                        || order.getStatus() == com.electroverse.entity.enums.OrderStatus.CONFIRMED,
                order.getCreatedAt()
        );
    }

    public static ShoppingResponses.Review toReview(Review review) {
        return new ShoppingResponses.Review(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getStatus().name(),
                review.getCreatedAt()
        );
    }

    public static ShoppingResponses.PriceAlert toPriceAlert(PriceAlert alert) {
        return new ShoppingResponses.PriceAlert(
                alert.getId(),
                alert.getProduct().getId(),
                alert.getProduct().getName(),
                alert.getProduct().getSlug(),
                alert.getTargetPrice(),
                alert.isActive(),
                alert.isTriggered(),
                alert.getTriggeredAt(),
                alert.getLastObservedPrice(),
                alert.getCreatedAt()
        );
    }

    public static FeatureResponses.Lifecycle toLifecycle(LifecycleAssessment a) {
        return new FeatureResponses.Lifecycle(
                a.getId(),
                a.getProduct().getId(),
                a.getProduct().getName(),
                a.getRepairability(),
                a.getUpgradeability(),
                a.getManufacturerSupport(),
                a.getWarranty(),
                a.getEnergyEfficiency(),
                a.getOverall(),
                a.getExplanation(),
                a.getAssessmentDate(),
                a.getMethodologyVersion(),
                a.isDemoData()
        );
    }

    public static FeatureResponses.CompatibilityRule toCompatibilityRule(CompatibilityRule rule) {
        return new FeatureResponses.CompatibilityRule(
                rule.getId(),
                rule.getRuleType(),
                rule.getSourceType(),
                rule.getTargetType(),
                rule.getSourceAttribute(),
                rule.getTargetAttribute(),
                rule.getExpectedValue(),
                rule.getRelation().name(),
                rule.getResult().name(),
                rule.getExplanation(),
                rule.isActive()
        );
    }

    public static AdminResponses.ReviewModeration toReviewModeration(Review review) {
        return new AdminResponses.ReviewModeration(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getStatus().name(),
                review.getCreatedAt()
        );
    }

    public static <T, R> CatalogResponses.Paged<R> toPaged(Page<T> page, java.util.function.Function<T, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).toList();
        return new CatalogResponses.Paged<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
