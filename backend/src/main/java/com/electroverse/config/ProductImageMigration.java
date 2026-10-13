package com.electroverse.config;

import com.electroverse.entity.Product;
import com.electroverse.entity.ProductImage;
import com.electroverse.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * One-time, idempotent correction of product image URLs that pointed at
 * unrelated random/placeholder stock photography (for example
 * {@code https://picsum.photos/seed/...}).
 *
 * <p>For every product and image whose stored URL is blank or comes from a
 * known random/placeholder host, the URL is replaced with the correct bundled
 * asset for the device. Products, customers, orders and all other data are left
 * untouched, prices are preserved, and genuine image URLs set by an
 * administrator are never overwritten. Re-running the migration is a no-op
 * because the corrected paths are recognised as reliable.</p>
 */
@Component
@Order(20)
public class ProductImageMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductImageMigration.class);

    private final ProductRepository productRepository;

    @Value("${app.images.migrate:true}")
    private boolean migrateEnabled;

    public ProductImageMigration(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!migrateEnabled) {
            return;
        }
        List<Product> products = productRepository.findAll();
        int correctedProducts = 0;
        int correctedImages = 0;

        for (Product product : products) {
            boolean changed = false;
            String resolved = ProductImageCatalog.resolve(product);

            if (ProductImageCatalog.isUnreliableUrl(product.getPrimaryImageUrl())) {
                product.setPrimaryImageUrl(resolved);
                changed = true;
            }
            String reliablePrimary = product.getPrimaryImageUrl();

            for (ProductImage image : product.getImages()) {
                if (ProductImageCatalog.isUnreliableUrl(image.getUrl())) {
                    image.setUrl(reliablePrimary);
                    correctedImages++;
                    changed = true;
                }
                if (image.getAltText() == null || image.getAltText().isBlank()) {
                    image.setAltText(product.getName());
                    changed = true;
                }
            }

            if (changed) {
                productRepository.save(product);
                correctedProducts++;
            }
        }

        if (correctedProducts > 0) {
            log.warn("ProductImageMigration: corrected image mappings for {} product(s) and {} image row(s). "
                    + "All broken/random remote URLs now resolve to bundled device assets.", correctedProducts, correctedImages);
        } else {
            log.info("ProductImageMigration: no unreliable product image URLs found; nothing to do.");
        }
    }
}
