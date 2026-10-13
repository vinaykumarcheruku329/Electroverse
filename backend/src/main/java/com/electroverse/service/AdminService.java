package com.electroverse.service;

import com.electroverse.dto.response.AdminResponses;
import com.electroverse.dto.response.AuthResponses;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.entity.Category;
import com.electroverse.entity.Order;
import com.electroverse.entity.Product;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.OrderStatus;
import com.electroverse.entity.enums.ReviewStatus;
import com.electroverse.entity.enums.Role;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.CategoryRepository;
import com.electroverse.repository.FinderSessionRepository;
import com.electroverse.repository.OrderRepository;
import com.electroverse.repository.PriceAlertRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.repository.ReviewRepository;
import com.electroverse.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final FinderSessionRepository finderSessionRepository;

    public AdminService(UserRepository userRepository,
                        ProductRepository productRepository,
                        CategoryRepository categoryRepository,
                        OrderRepository orderRepository,
                        ReviewRepository reviewRepository,
                        PriceAlertRepository priceAlertRepository,
                        FinderSessionRepository finderSessionRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.finderSessionRepository = finderSessionRepository;
    }

    @Transactional(readOnly = true)
    public AdminResponses.Dashboard dashboard() {
        List<Order> allOrders = orderRepository.findAll();
        BigDecimal revenue = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminResponses.CountItem> ordersByStatus = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = allOrders.stream().filter(o -> o.getStatus() == status).count();
            ordersByStatus.add(new AdminResponses.CountItem(status.name(), count));
        }

        List<AdminResponses.CountItem> productsByCategory = new ArrayList<>();
        for (Category category : categoryRepository.findAllByOrderByNameAsc()) {
            productsByCategory.add(new AdminResponses.CountItem(
                    category.getName(), productRepository.countByCategoryId(category.getId())));
        }

        List<com.electroverse.dto.response.ShoppingResponses.Order> recent = orderRepository
                .findTop10ByOrderByCreatedAtDesc().stream()
                .map(DtoMapper::toOrder).toList();

        return new AdminResponses.Dashboard(
                userRepository.count(),
                userRepository.countByRole(Role.CUSTOMER),
                productRepository.count(),
                productRepository.findAll().stream().filter(Product::isActive).count(),
                allOrders.size(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.DELIVERED),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                revenue,
                productRepository.countByStockLessThanEqual(LOW_STOCK_THRESHOLD),
                reviewRepository.count(),
                reviewRepository.countByStatus(ReviewStatus.PENDING),
                priceAlertRepository.countByTriggeredTrue(),
                finderSessionRepository.count(),
                ordersByStatus,
                productsByCategory,
                recent
        );
    }

    @Transactional(readOnly = true)
    public CatalogResponses.Paged<AuthResponses.User> listUsers(int page, int size) {
        Page<User> users = userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, Math.min(size, 60)));
        return DtoMapper.toPaged(users, DtoMapper::toUser);
    }

    @Transactional
    public AuthResponses.User setUserActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(active);
        return DtoMapper.toUser(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AdminResponses.InventoryRow> inventory(boolean lowStockOnly) {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "stock"));
        return products.stream()
                .filter(p -> !lowStockOnly || p.getStock() <= LOW_STOCK_THRESHOLD)
                .map(this::toInventoryRow)
                .toList();
    }

    private AdminResponses.InventoryRow toInventoryRow(Product product) {
        String status;
        if (product.getStock() <= 0) {
            status = "OUT_OF_STOCK";
        } else if (product.getStock() <= LOW_STOCK_THRESHOLD) {
            status = "LOW_STOCK";
        } else {
            status = "IN_STOCK";
        }
        return new AdminResponses.InventoryRow(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getStock(),
                product.getPrice(),
                product.isActive(),
                status
        );
    }

    public static int getLowStockThreshold() {
        return LOW_STOCK_THRESHOLD;
    }
}
