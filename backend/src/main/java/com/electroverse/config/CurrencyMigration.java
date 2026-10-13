package com.electroverse.config;

import com.electroverse.entity.Order;
import com.electroverse.entity.OrderItem;
import com.electroverse.entity.PriceAlert;
import com.electroverse.entity.PriceHistory;
import com.electroverse.entity.Product;
import com.electroverse.repository.OrderRepository;
import com.electroverse.repository.PriceAlertRepository;
import com.electroverse.repository.PriceHistoryRepository;
import com.electroverse.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * One-time, idempotent conversion of pre-existing USD records to INR.
 *
 * ElectroVerse now operates in Indian Rupees. Any records that were originally
 * stored in USD are converted at the documented rate below rather than being
 * relabelled. The conversion only runs when the whole catalogue is still USD, so
 * a mixed (already partly converted) catalogue is never double-converted. Every
 * change is logged and the migration no-ops once no USD products remain.
 */
@Component
@org.springframework.core.annotation.Order
public class CurrencyMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CurrencyMigration.class);

    private static final BigDecimal USD_TO_INR = new BigDecimal("83.00");
    private static final String USD = "USD";
    private static final String INR = "INR";

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final OrderRepository orderRepository;

    @Value("${app.currency.migrate-usd-to-inr:true}")
    private boolean migrateEnabled;

    public CurrencyMigration(ProductRepository productRepository,
                             PriceHistoryRepository priceHistoryRepository,
                             PriceAlertRepository priceAlertRepository,
                             OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!migrateEnabled) {
            return;
        }
        List<Product> allProducts = productRepository.findAll();
        List<Product> usdProducts = allProducts.stream()
                .filter(p -> USD.equalsIgnoreCase(p.getCurrency()))
                .toList();
        if (usdProducts.isEmpty()) {
            return;
        }
        if (usdProducts.size() != allProducts.size()) {
            log.warn("CurrencyMigration skipped: found a mixed catalogue ({} of {} products are USD). "
                    + "Convert or reseed ambiguous records manually.", usdProducts.size(), allProducts.size());
            return;
        }

        log.warn("CurrencyMigration: converting {} USD product(s) to INR at the documented rate 1 USD = {} INR. "
                + "Monetary history (price history, alerts, orders) is converted consistently.", usdProducts.size(), USD_TO_INR);

        for (Product product : usdProducts) {
            product.setPrice(convert(product.getPrice()));
            if (product.getCompareAtPrice() != null) {
                product.setCompareAtPrice(convert(product.getCompareAtPrice()));
            }
            product.setCurrency(INR);
        }
        productRepository.saveAll(usdProducts);

        List<PriceHistory> histories = priceHistoryRepository.findAll();
        for (PriceHistory history : histories) {
            history.setPrice(convert(history.getPrice()));
        }
        if (!histories.isEmpty()) {
            priceHistoryRepository.saveAll(histories);
        }

        List<PriceAlert> alerts = priceAlertRepository.findAll();
        for (PriceAlert alert : alerts) {
            alert.setTargetPrice(convert(alert.getTargetPrice()));
            if (alert.getLastObservedPrice() != null) {
                alert.setLastObservedPrice(convert(alert.getLastObservedPrice()));
            }
        }
        if (!alerts.isEmpty()) {
            priceAlertRepository.saveAll(alerts);
        }

        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            order.setSubtotal(convert(order.getSubtotal()));
            order.setShippingFee(convert(order.getShippingFee()));
            order.setTax(convert(order.getTax()));
            order.setTotal(convert(order.getTotal()));
            for (OrderItem item : order.getItems()) {
                item.setUnitPrice(convert(item.getUnitPrice()));
                item.setLineTotal(convert(item.getLineTotal()));
            }
        }
        if (!orders.isEmpty()) {
            orderRepository.saveAll(orders);
        }

        log.warn("CurrencyMigration complete: {} product(s), {} price snapshot(s), {} alert(s), {} order(s) converted.",
                usdProducts.size(), histories.size(), alerts.size(), orders.size());
    }

    private BigDecimal convert(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.multiply(USD_TO_INR).setScale(2, RoundingMode.HALF_UP);
    }
}
