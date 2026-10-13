package com.electroverse.config;

import com.electroverse.entity.Category;
import com.electroverse.entity.CompatibilityRule;
import com.electroverse.entity.LifecycleAssessment;
import com.electroverse.entity.PriceHistory;
import com.electroverse.entity.Product;
import com.electroverse.entity.ProductImage;
import com.electroverse.entity.ProductSpec;
import com.electroverse.entity.Review;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.CompatibilityRelation;
import com.electroverse.entity.enums.CompatibilityResult;
import com.electroverse.entity.enums.ReviewStatus;
import com.electroverse.entity.enums.Role;
import com.electroverse.repository.CategoryRepository;
import com.electroverse.repository.CompatibilityRuleRepository;
import com.electroverse.repository.LifecycleAssessmentRepository;
import com.electroverse.repository.PriceHistoryRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.repository.ReviewRepository;
import com.electroverse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /**
     * Development seed prices are authored in USD and converted to INR at this
     * documented rate so that seeded data is genuinely converted rather than relabelled.
     */
    private static final BigDecimal USD_TO_INR = new BigDecimal("83.00");

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CompatibilityRuleRepository compatibilityRuleRepository;
    private final LifecycleAssessmentRepository lifecycleRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@electroverse.local}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      CompatibilityRuleRepository compatibilityRuleRepository,
                      LifecycleAssessmentRepository lifecycleRepository,
                      PriceHistoryRepository priceHistoryRepository,
                      ReviewRepository reviewRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.compatibilityRuleRepository = compatibilityRuleRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedAdmin();
        if (seedEnabled && productRepository.count() == 0) {
            seedCatalog();
        }
    }

    private void seedAdmin() throws Exception {
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setEmail(adminEmail.toLowerCase());
        admin.setFullName("ElectroVerse Administrator");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        if (adminPassword != null && !adminPassword.isBlank()) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            log.info("Initial administrator '{}' created from configured password.", adminEmail);
        } else {
            String generated = generatePassword();
            admin.setPassword(passwordEncoder.encode(generated));
            userRepository.save(admin);
            Path file = Path.of(System.getProperty("user.dir"), "admin-credentials.txt");
            Files.writeString(file, "email=" + adminEmail + System.lineSeparator() + "password=" + generated
                    + System.lineSeparator() + "generated=" + LocalDateTime.now() + System.lineSeparator());
            log.warn("Initial administrator created with a generated password. "
                    + "Credentials written to {} (git-ignored). Change the password after first login.", file.toAbsolutePath());
        }
    }

    private void seedCatalog() {
        Map<String, Category> categories = new LinkedHashMap<>();
        categories.put("laptops", category("Laptops", "laptops", "High-performance notebooks and ultrabooks", "laptop"));
        categories.put("smartphones", category("Smartphones", "smartphones", "Flagship and mid-range phones", "smartphone"));
        categories.put("gpus", category("Graphics Cards", "gpus", "Discrete GPUs for gaming and rendering", "gpu"));
        categories.put("cpus", category("Processors", "cpus", "Desktop and workstation CPUs", "cpu"));
        categories.put("monitors", category("Monitors", "monitors", "Displays for work and play", "monitor"));
        categories.put("ram", category("Memory", "ram", "DDR4 and DDR5 memory kits", "ram"));
        categories.put("storage", category("Storage", "storage", "SSDs and NVMe drives", "storage"));
        categories.put("chargers", category("Chargers & Power", "chargers", "Adapters, PSUs and charging bricks", "charger"));
        categories.put("accessories", category("Accessories", "accessories", "Keyboards, mice and more", "accessory"));

        List<Product> products = new ArrayList<>();
        products.add(product("Aurora X16 Creator Laptop", "Aurora", categories.get("laptops"), "1499.00", "1699.00", 18, 24,
                "A 16-inch creator notebook with a colour-accurate display and strong multi-core performance for programming and content work.",
                true, LocalDate.of(2025, 3, 14),
                specs(spec("Compute", "Processor", "Aurora A9 12-core", null),
                        spec("Compute", "RAM (GB)", "32", 32.0),
                        spec("Compute", "Storage (GB)", "1000", 1000.0),
                        spec("Compute", "Benchmark Score", "1180", 1180.0),
                        spec("Display", "Display", "16.0 inch 3K 120Hz", 120.0),
                        spec("Power", "Battery Life (hrs)", "13", 13.0),
                        spec("Chassis", "Weight (kg)", "1.8", 1.8),
                        spec("Power", "Energy", "efficient 65W", 65.0))));

        products.add(product("Aurora Swift 14 Ultrabook", "Aurora", categories.get("laptops"), "949.00", "1049.00", 25, 24,
                "Lightweight 14-inch ultrabook with all-day battery life, ideal for study and office work.",
                true, LocalDate.of(2025, 1, 20),
                specs(spec("Compute", "Processor", "Aurora U7 8-core", null),
                        spec("Compute", "RAM (GB)", "16", 16.0),
                        spec("Compute", "Storage (GB)", "512", 512.0),
                        spec("Compute", "Benchmark Score", "860", 860.0),
                        spec("Display", "Display", "14.0 inch 2.2K 90Hz", 90.0),
                        spec("Power", "Battery Life (hrs)", "18", 18.0),
                        spec("Chassis", "Weight (kg)", "1.1", 1.1),
                        spec("Power", "Energy", "efficient 45W", 45.0))));

        products.add(product("Nova Gaming 17 RTX Laptop", "Nova", categories.get("laptops"), "2199.00", "2399.00", 9, 24,
                "17-inch gaming laptop with a high-refresh display and top-tier discrete graphics.",
                false, LocalDate.of(2025, 5, 2),
                specs(spec("Compute", "Processor", "Nova HX 16-core", null),
                        spec("Compute", "RAM (GB)", "32", 32.0),
                        spec("Compute", "Storage (GB)", "2000", 2000.0),
                        spec("Compute", "Benchmark Score", "1420", 1420.0),
                        spec("Display", "Display", "17.3 inch QHD 240Hz", 240.0),
                        spec("Power", "Battery Life (hrs)", "7", 7.0),
                        spec("Chassis", "Weight (kg)", "2.9", 2.9),
                        spec("Power", "Energy", "high power 240W", 240.0))));

        products.add(product("Vertex Zen 14 Business Laptop", "Vertex", categories.get("laptops"), "1149.00", null, 14, 36,
                "Durable business laptop with excellent keyboard, security features and long warranty.",
                false, LocalDate.of(2024, 11, 8),
                specs(spec("Compute", "Processor", "Vertex V9 10-core", null),
                        spec("Compute", "RAM (GB)", "16", 16.0),
                        spec("Compute", "Storage (GB)", "512", 512.0),
                        spec("Compute", "Benchmark Score", "940", 940.0),
                        spec("Display", "Display", "14.0 inch FHD+ 60Hz", 60.0),
                        spec("Power", "Battery Life (hrs)", "16", 16.0),
                        spec("Chassis", "Weight (kg)", "1.35", 1.35),
                        spec("Power", "Energy", "efficient 55W", 55.0))));

        products.add(product("Helio Pro Max 5G", "Helio", categories.get("smartphones"), "1099.00", "1199.00", 40, 24,
                "Flagship smartphone with a pro-grade camera system and exceptional display.",
                true, LocalDate.of(2025, 2, 10),
                specs(spec("Compute", "Processor", "Helio H3", null),
                        spec("Compute", "RAM (GB)", "12", 12.0),
                        spec("Compute", "Storage (GB)", "256", 256.0),
                        spec("Compute", "Benchmark Score", "1500", 1500.0),
                        spec("Display", "Display", "6.8 inch LTPO 120Hz", 120.0),
                        spec("Power", "Battery (mAh)", "5000", 5000.0),
                        spec("Chassis", "Weight (kg)", "0.22", 0.22))));

        products.add(product("Helio Air 5G", "Helio", categories.get("smartphones"), "649.00", "699.00", 55, 24,
                "Balanced 5G phone with great battery life for everyday use.",
                false, LocalDate.of(2025, 4, 1),
                specs(spec("Compute", "Processor", "Helio H2", null),
                        spec("Compute", "RAM (GB)", "8", 8.0),
                        spec("Compute", "Storage (GB)", "128", 128.0),
                        spec("Compute", "Benchmark Score", "980", 980.0),
                        spec("Display", "Display", "6.5 inch OLED 90Hz", 90.0),
                        spec("Power", "Battery (mAh)", "4800", 4800.0),
                        spec("Chassis", "Weight (kg)", "0.19", 0.19))));

        products.add(product("Pulse Vantage GPU 16GB", "Pulse", categories.get("gpus"), "899.00", "1049.00", 12, 36,
                "High-end graphics card for 1440p and 4K gaming.",
                true, LocalDate.of(2025, 1, 30),
                specs(spec("Graphics", "GPU Chip", "PV-G80", null),
                        spec("Graphics", "VRAM (GB)", "16", 16.0),
                        spec("Power", "Recommended PSU (W)", "750", 750.0),
                        spec("Power", "TDP (W)", "320", 320.0),
                        spec("Compute", "Benchmark Score", "1900", 1900.0))));

        products.add(product("Pulse Spark GPU 8GB", "Pulse", categories.get("gpus"), "449.00", "499.00", 22, 36,
                "Efficient mid-range GPU, ideal for 1080p gaming.",
                false, LocalDate.of(2024, 10, 15),
                specs(spec("Graphics", "GPU Chip", "PS-G60", null),
                        spec("Graphics", "VRAM (GB)", "8", 8.0),
                        spec("Power", "Recommended PSU (W)", "550", 550.0),
                        spec("Power", "TDP (W)", "180", 180.0),
                        spec("Compute", "Benchmark Score", "1200", 1200.0))));

        products.add(product("Core Titan CPU 16-Core", "Cortex", categories.get("cpus"), "559.00", null, 30, 36,
                "Desktop processor with 16 cores for heavy multitasking and rendering.",
                false, LocalDate.of(2025, 3, 5),
                specs(spec("Compute", "Socket", "AM5", null),
                        spec("Compute", "Cores", "16", 16.0),
                        spec("Compute", "Threads", "32", 32.0),
                        spec("Power", "TDP (W)", "170", 170.0),
                        spec("Compute", "Benchmark Score", "1650", 1650.0))));

        products.add(product("Core Sprint CPU 8-Core", "Cortex", categories.get("cpus"), "279.00", "319.00", 44, 36,
                "Efficient 8-core processor for mainstream desktops.",
                false, LocalDate.of(2024, 9, 22),
                specs(spec("Compute", "Socket", "AM5", null),
                        spec("Compute", "Cores", "8", 8.0),
                        spec("Compute", "Threads", "16", 16.0),
                        spec("Power", "TDP (W)", "105", 105.0),
                        spec("Compute", "Benchmark Score", "1100", 1100.0))));

        products.add(product("Vision 27 4K Monitor", "Vision", categories.get("monitors"), "429.00", "479.00", 26, 36,
                "27-inch 4K IPS monitor with USB-C connectivity.",
                true, LocalDate.of(2025, 2, 18),
                specs(spec("Display", "Screen Size (in)", "27", 27.0),
                        spec("Display", "Resolution", "3840x2160", 2160.0),
                        spec("Display", "Refresh Rate (Hz)", "60", 60.0),
                        spec("Display", "Panel", "IPS", null),
                        spec("Ports", "Ports", "HDMI, DisplayPort, USB-C", null))));

        products.add(product("Vision 32 Curved 165Hz", "Vision", categories.get("monitors"), "549.00", null, 15, 24,
                "32-inch curved gaming monitor with a fast refresh rate.",
                false, LocalDate.of(2024, 12, 1),
                specs(spec("Display", "Screen Size (in)", "32", 32.0),
                        spec("Display", "Resolution", "2560x1440", 1440.0),
                        spec("Display", "Refresh Rate (Hz)", "165", 165.0),
                        spec("Display", "Panel", "VA", null),
                        spec("Ports", "Ports", "HDMI, DisplayPort", null))));

        products.add(product("Vertex DDR5 32GB Kit", "Vertex", categories.get("ram"), "139.00", "159.00", 60, 60,
                "High-speed DDR5 memory kit for modern platforms.",
                false, LocalDate.of(2025, 1, 12),
                specs(spec("Memory", "Memory Type", "DDR5", null),
                        spec("Memory", "Capacity (GB)", "32", 32.0),
                        spec("Memory", "Speed (MHz)", "6000", 6000.0))));

        products.add(product("Vertex DDR4 16GB Kit", "Vertex", categories.get("ram"), "69.00", null, 70, 60,
                "Reliable DDR4 memory kit for value builds.",
                false, LocalDate.of(2023, 6, 30),
                specs(spec("Memory", "Memory Type", "DDR4", null),
                        spec("Memory", "Capacity (GB)", "16", 16.0),
                        spec("Memory", "Speed (MHz)", "3200", 3200.0))));

        products.add(product("Flash NVMe 2TB SSD", "Flash", categories.get("storage"), "189.00", "219.00", 48, 60,
                "Fast PCIe 4.0 NVMe SSD for gaming and content creation.",
                true, LocalDate.of(2025, 2, 25),
                specs(spec("Storage", "Interface", "NVMe PCIe 4.0", null),
                        spec("Storage", "Capacity (GB)", "2000", 2000.0),
                        spec("Storage", "Form Factor", "M.2 2280", null),
                        spec("Storage", "Read Speed (MB/s)", "7000", 7000.0))));

        products.add(product("Flash SATA 1TB SSD", "Flash", categories.get("storage"), "79.00", null, 65, 36,
                "Dependable SATA SSD for upgrades and boot drives.",
                false, LocalDate.of(2024, 5, 19),
                specs(spec("Storage", "Interface", "SATA III", null),
                        spec("Storage", "Capacity (GB)", "1000", 1000.0),
                        spec("Storage", "Form Factor", "2.5 inch", null),
                        spec("Storage", "Read Speed (MB/s)", "550", 550.0))));

        products.add(product("Flux 100W USB-C Charger", "Flux", categories.get("chargers"), "59.00", "69.00", 80, 24,
                "Compact GaN USB-C charger with multiple ports.",
                false, LocalDate.of(2025, 3, 8),
                specs(spec("Power", "Connector", "USB-C", null),
                        spec("Power", "Wattage (W)", "100", 100.0))));

        products.add(product("Flux 65W Laptop Adapter", "Flux", categories.get("chargers"), "39.00", "45.00", 90, 24,
                "Universal 65W laptop adapter with USB-C PD.",
                false, LocalDate.of(2024, 8, 14),
                specs(spec("Power", "Connector", "USB-C", null),
                        spec("Power", "Wattage (W)", "65", 65.0))));

        products.add(product("Flux 750W Gold PSU", "Flux", categories.get("chargers"), "109.00", null, 30, 60,
                "80 Plus Gold modular power supply for gaming builds.",
                false, LocalDate.of(2025, 1, 5),
                specs(spec("Power", "Connector", "ATX 24-pin", null),
                        spec("Power", "Wattage (W)", "750", 750.0),
                        spec("Power", "Efficiency Rating", "80 Plus Gold", null))));

        products.add(product("Nova Mechanical Keyboard", "Nova", categories.get("accessories"), "119.00", "139.00", 55, 24,
                "Hot-swappable mechanical keyboard with RGB lighting.",
                false, LocalDate.of(2025, 4, 12),
                specs(spec("Accessory", "Type", "Mechanical Keyboard", null),
                        spec("Accessory", "Connection", "USB-C / Wireless", null))));

        products.add(product("Nova Precision Mouse", "Nova", categories.get("accessories"), "69.00", "79.00", 72, 24,
                "Lightweight wireless gaming mouse with a high-precision sensor.",
                false, LocalDate.of(2025, 4, 12),
                specs(spec("Accessory", "Type", "Wireless Mouse", null),
                        spec("Accessory", "DPI", "26000", 26000.0))));

        productRepository.saveAll(products);

        seedCompatibilityRules();
        seedLifecycle(products);
        seedPriceHistory(products);
        seedReviews(products);
        log.info("Seeded {} categories and {} products (development demo catalog).", categories.size(), products.size());
    }

    private void seedCompatibilityRules() {
        if (compatibilityRuleRepository.count() > 0) {
            return;
        }
        List<CompatibilityRule> rules = new ArrayList<>();
        rules.add(rule("CPU_MOTHERBOARD", "CPU", "MOTHERBOARD", "Socket", "Socket", null,
                CompatibilityRelation.EQUALS, CompatibilityResult.COMPATIBLE,
                "The CPU socket matches the motherboard socket."));
        rules.add(rule("RAM_MOTHERBOARD", "RAM", "MOTHERBOARD", "Memory Type", "Memory Type", null,
                CompatibilityRelation.EQUALS, CompatibilityResult.COMPATIBLE,
                "The memory type (DDR4/DDR5) matches the motherboard memory support."));
        rules.add(rule("GPU_PSU", "GPU", "PSU", "Recommended PSU (W)", "Wattage (W)", null,
                CompatibilityRelation.LESS_OR_EQUAL, CompatibilityResult.COMPATIBLE,
                "The power supply wattage meets or exceeds the GPU recommended PSU wattage."));
        rules.add(rule("CHARGER_LAPTOP", "CHARGER", "LAPTOP", "Connector", "Charging Port", null,
                CompatibilityRelation.EQUALS, CompatibilityResult.COMPATIBLE,
                "The charger connector matches the laptop charging port."));
        rules.add(rule("MONITOR_CONNECTION", "MONITOR", "CONNECTION", "Ports", null, "HDMI",
                CompatibilityRelation.CONTAINS, CompatibilityResult.COMPATIBLE,
                "The monitor provides the required HDMI connection."));
        rules.add(rule("MONITOR_CONNECTION", "MONITOR", "CONNECTION", "Ports", null, "DisplayPort",
                CompatibilityRelation.CONTAINS, CompatibilityResult.CONDITIONAL,
                "The monitor supports DisplayPort; a compatible cable and source are required."));
        compatibilityRuleRepository.saveAll(rules);
    }

    private void seedLifecycle(List<Product> products) {
        if (lifecycleRepository.count() > 0) {
            return;
        }
        List<LifecycleAssessment> assessments = new ArrayList<>();
        assessments.add(lifecycle(products.get(0), 78, 82, 85, 80, 74));
        assessments.add(lifecycle(products.get(1), 72, 60, 88, 80, 88));
        assessments.add(lifecycle(products.get(2), 65, 70, 80, 80, 55));
        assessments.add(lifecycle(products.get(3), 84, 68, 92, 90, 82));
        assessments.add(lifecycle(products.get(4), 55, 30, 90, 80, 70));
        assessments.add(lifecycle(products.get(8), 70, 88, 78, 85, 60));
        lifecycleRepository.saveAll(assessments);
    }

    private void seedPriceHistory(List<Product> products) {
        if (priceHistoryRepository.count() > 0) {
            return;
        }
        List<PriceHistory> snapshots = new ArrayList<>();
        for (Product product : products) {
            BigDecimal base = product.getPrice();
            for (int i = 5; i >= 0; i--) {
                double swing = (i % 2 == 0 ? 1.03 : 0.97) * (1 + (i * 0.005));
                BigDecimal price = base.multiply(BigDecimal.valueOf(swing)).setScale(2, RoundingMode.HALF_UP);
                snapshots.add(snapshot(product, price, LocalDateTime.now().minusDays(i * 14L).withHour(9).withMinute(0)));
            }
            snapshots.add(snapshot(product, base, LocalDateTime.now()));
        }
        priceHistoryRepository.saveAll(snapshots);
    }

    private void seedReviews(List<Product> products) {
        if (reviewRepository.count() > 0) {
            return;
        }
        User customer = new User();
        customer.setEmail("demo.customer@electroverse.local");
        customer.setFullName("Demo Customer");
        customer.setPassword(passwordEncoder.encode("DemoPass123"));
        customer.setRole(Role.CUSTOMER);
        customer.setActive(true);
        customer = userRepository.save(customer);

        reviewRepository.save(review(customer, products.get(0), 5, "Excellent for development", "Compiles large projects quickly and the display is superb."));
        reviewRepository.save(review(customer, products.get(1), 4, "Great ultrabook", "Light and the battery truly lasts a full day."));
        reviewRepository.save(review(customer, products.get(4), 5, "Best camera yet", "Night photos are impressively detailed."));
    }

    private Category category(String name, String slug, String description, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setIcon(icon);
        return categoryRepository.save(category);
    }

    private Product product(String name, String brand, Category category, String price, String compareAt,
                            int stock, int warrantyMonths, String description, boolean featured,
                            LocalDate releaseDate, List<ProductSpec> specList) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(slugify(name));
        product.setBrand(brand);
        product.setCategory(category);
        product.setPrice(toInr(price));
        if (compareAt != null) {
            product.setCompareAtPrice(toInr(compareAt));
        }
        product.setCurrency("INR");
        product.setStock(stock);
        product.setWarrantyMonths(warrantyMonths);
        product.setDescription(description);
        product.setFeatured(featured);
        product.setReleaseDate(releaseDate);
        product.setPopularity(100 - stock);
        product.setPrimaryImageUrl(ProductImageCatalog.resolve(product));
        product.setRatingAverage(0.0);
        product.setRatingCount(0);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(product.getPrimaryImageUrl());
        image.setAltText(name);
        image.setSortOrder(0);
        product.getImages().add(image);

        for (ProductSpec spec : specList) {
            spec.setProduct(product);
            product.getSpecs().add(spec);
        }
        return product;
    }

    private CompatibilityRule rule(String ruleType, String sourceType, String targetType,
                                   String sourceAttribute, String targetAttribute, String expectedValue,
                                   CompatibilityRelation relation, CompatibilityResult result, String explanation) {
        CompatibilityRule rule = new CompatibilityRule();
        rule.setRuleType(ruleType);
        rule.setSourceType(sourceType);
        rule.setTargetType(targetType);
        rule.setSourceAttribute(sourceAttribute);
        rule.setTargetAttribute(targetAttribute);
        rule.setExpectedValue(expectedValue);
        rule.setRelation(relation);
        rule.setResult(result);
        rule.setExplanation(explanation);
        rule.setActive(true);
        return rule;
    }

    private LifecycleAssessment lifecycle(Product product, int repair, int upgrade, int support, int warranty, int energy) {
        LifecycleAssessment assessment = new LifecycleAssessment();
        assessment.setProduct(product);
        assessment.setRepairability(repair);
        assessment.setUpgradeability(upgrade);
        assessment.setManufacturerSupport(support);
        assessment.setWarranty(warranty);
        assessment.setEnergyEfficiency(energy);
        double overall = repair * 0.25 + upgrade * 0.20 + support * 0.20 + warranty * 0.15 + energy * 0.20;
        assessment.setOverall(BigDecimal.valueOf(overall).setScale(1, RoundingMode.HALF_UP).doubleValue());
        assessment.setExplanation("Demo assessment (unverified sample data) computed with methodology v1.0 from stored evidence.");
        assessment.setAssessmentDate(LocalDate.now().minusMonths(1));
        assessment.setMethodologyVersion("1.0");
        assessment.setDemoData(true);
        return assessment;
    }

    private PriceHistory snapshot(Product product, BigDecimal price, LocalDateTime when) {
        PriceHistory history = new PriceHistory();
        history.setProduct(product);
        history.setPrice(price);
        history.setRecordedAt(when);
        history.setSource("demo-seed");
        history.setDemoData(true);
        return history;
    }

    private Review review(User user, Product product, int rating, String title, String comment) {
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setTitle(title);
        review.setComment(comment);
        review.setStatus(ReviewStatus.APPROVED);
        return review;
    }

    private List<ProductSpec> specs(ProductSpec... specs) {
        return new ArrayList<>(List.of(specs));
    }

    private ProductSpec spec(String group, String key, String value, Double numeric) {
        ProductSpec spec = new ProductSpec();
        spec.setSpecGroup(group);
        spec.setSpecKey(key);
        spec.setSpecValue(value);
        spec.setNumericValue(numeric);
        return spec;
    }

    private BigDecimal toInr(String usdAmount) {
        return new BigDecimal(usdAmount).multiply(USD_TO_INR).setScale(2, RoundingMode.HALF_UP);
    }

    private String slugify(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String generatePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
