package com.electroverse.config;

import com.electroverse.entity.Product;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Single source of truth for mapping a product to a bundled, device-accurate
 * image asset served by the frontend from {@code /images/products/}.
 *
 * <p>Images are matched by device keywords in the product name first (so a
 * gaming laptop gets a gaming-laptop packshot, a business laptop gets a
 * business-laptop packshot, and so on) and then by category as a fallback.
 * Nothing here relies on random stock-photo services.</p>
 */
public final class ProductImageCatalog {

    public static final String BASE_PATH = "/images/products/";
    public static final String PLACEHOLDER = "placeholder.svg";

    private record Rule(Pattern pattern, String file) {
    }

    private static final Map<String, String> CATEGORY_FALLBACK = Map.of(
            "laptops", "laptop.svg",
            "smartphones", "smartphone.svg",
            "gpus", "graphics-card.svg",
            "cpus", "processor.svg",
            "monitors", "monitor.svg",
            "ram", "ram.svg",
            "storage", "ssd.svg",
            "chargers", "charger.svg",
            "accessories", "keyboard.svg"
    );

    private static final List<Rule> RULES = List.of(
            rule("gaming", "gaming-laptop.svg"),
            rule("creator", "creator-laptop.svg"),
            rule("ultrabook|\\bswift\\b", "ultrabook.svg"),
            rule("business", "business-laptop.svg"),
            rule("\\bpsu\\b|power supply", "psu.svg"),
            rule("adapter", "laptop-adapter.svg"),
            rule("charger|charging", "charger.svg"),
            rule("keyboard", "keyboard.svg"),
            rule("mouse", "mouse.svg"),
            rule("graphics|\\bgpu\\b|geforce|radeon", "graphics-card.svg"),
            rule("processor|\\bcpu\\b|ryzen", "processor.svg"),
            rule("monitor|display", "monitor.svg"),
            rule("\\bddr\\d?\\b|\\bram\\b|memory", "ram.svg"),
            rule("\\bssd\\b|nvme|sata|storage", "ssd.svg"),
            rule("smartphone|\\bphone\\b|\\b5g\\b|galaxy|iphone|pixel", "smartphone.svg"),
            rule("laptop|notebook|macbook", "laptop.svg")
    );

    private static final List<String> UNRELIABLE_MARKERS = List.of(
            "picsum.photos", "loremflickr", "lorempixel", "placehold.co",
            "placeholder.com", "via.placeholder", "dummyimage",
            "source.unsplash.com", "unsplash.com/featured"
    );

    private ProductImageCatalog() {
    }

    private static Rule rule(String regex, String file) {
        return new Rule(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), file);
    }

    /**
     * Resolves the correct bundled asset path for a product.
     *
     * @return an absolute path such as {@code /images/products/gaming-laptop.svg}
     */
    public static String resolve(Product product) {
        if (product == null) {
            return BASE_PATH + PLACEHOLDER;
        }
        String name = product.getName() == null ? "" : product.getName();
        for (Rule rule : RULES) {
            if (rule.pattern().matcher(name).find()) {
                return BASE_PATH + rule.file();
            }
        }
        String slug = product.getCategory() != null && product.getCategory().getSlug() != null
                ? product.getCategory().getSlug().toLowerCase()
                : "";
        return BASE_PATH + CATEGORY_FALLBACK.getOrDefault(slug, PLACEHOLDER);
    }

    /**
     * Whether a stored URL is blank or comes from a random/placeholder image
     * service that returns unrelated photography. Such URLs are safe to correct
     * because they were never genuine product images; a valid custom URL set by
     * an administrator is never touched.
     */
    public static boolean isUnreliableUrl(String url) {
        if (url == null || url.isBlank()) {
            return true;
        }
        String lower = url.toLowerCase();
        return UNRELIABLE_MARKERS.stream().anyMatch(lower::contains);
    }
}
