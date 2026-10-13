package com.electroverse;

import com.electroverse.entity.Category;
import com.electroverse.entity.Order;
import com.electroverse.entity.Product;
import com.electroverse.entity.ProductSpec;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.OrderStatus;
import com.electroverse.entity.enums.PaymentMethod;
import com.electroverse.entity.enums.Role;
import com.electroverse.repository.CategoryRepository;
import com.electroverse.repository.OrderRepository;
import com.electroverse.repository.ProductRepository;
import com.electroverse.repository.UserRepository;
import com.electroverse.security.JwtService;
import com.electroverse.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ElectroVerseApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String customerToken;
    private String adminToken;
    private Long productId;
    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setEmail("integration.customer@test.local");
        customer.setFullName("Integration Customer");
        customer.setPassword(passwordEncoder.encode("CustomerPass123"));
        customer.setRole(Role.CUSTOMER);
        customer.setActive(true);
        customer = userRepository.save(customer);
        customerToken = jwtService.generateToken(new UserPrincipal(customer));

        User admin = new User();
        admin.setEmail("integration.admin@test.local");
        admin.setFullName("Integration Admin");
        admin.setPassword(passwordEncoder.encode("AdminPass123"));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin = userRepository.save(admin);
        adminToken = jwtService.generateToken(new UserPrincipal(admin));

        Category category = new Category();
        category.setName("Test Laptops");
        category.setSlug("test-laptops");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Integration Laptop");
        product.setSlug("integration-laptop");
        product.setBrand("TestBrand");
        product.setCategory(category);
        product.setDescription("A laptop for integration tests.");
        product.setPrice(new BigDecimal("1200.00"));
        product.setCurrency("INR");
        product.setStock(5);
        product.setActive(true);
        product.setFeatured(true);
        product.setWarrantyMonths(24);
        ProductSpec spec = new ProductSpec();
        spec.setSpecGroup("Compute");
        spec.setSpecKey("Benchmark Score");
        spec.setSpecValue("1000");
        spec.setNumericValue(1000.0);
        spec.setProduct(product);
        product.getSpecs().add(spec);
        product = productRepository.save(product);
        productId = product.getId();
    }

    @Test
    void registerThenAccessProfile() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "newuser@test.local",
                "password", "NewPass12345",
                "fullName", "New User",
                "phone", "9876543210"));

        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@test.local"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "not-an-email",
                "password", "short",
                "fullName", ""));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void publicRegistrationCannotAssignAdminRole() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "wannabe.admin@test.local",
                "password", "NewPass12345",
                "fullName", "Wannabe Admin",
                "phone", "9876500000",
                "role", "ADMIN"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));

        assertThat(userRepository.findByEmailIgnoreCase("wannabe.admin@test.local").orElseThrow().getRole())
                .isEqualTo(Role.CUSTOMER);
    }

    @Test
    void validLoginSucceedsAndInvalidCredentialsAreRejected() throws Exception {
        String okBody = objectMapper.writeValueAsString(Map.of(
                "email", "integration.customer@test.local",
                "password", "CustomerPass123"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(okBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.password").doesNotExist());

        String badBody = objectMapper.writeValueAsString(Map.of(
                "email", "integration.customer@test.local",
                "password", "WrongPassword123"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(badBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        User disabled = new User();
        disabled.setEmail("disabled@test.local");
        disabled.setFullName("Disabled User");
        disabled.setPassword(passwordEncoder.encode("DisabledPass123"));
        disabled.setRole(Role.CUSTOMER);
        disabled.setActive(false);
        userRepository.save(disabled);

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "disabled@test.local",
                "password", "DisabledPass123"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotAccessAnotherCustomersOrder() throws Exception {
        Order order = new Order();
        order.setUser(customer);
        order.setOrderNumber("EV-TEST-000001");
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        order.setSubtotal(new BigDecimal("1000.00"));
        order.setShippingFee(new BigDecimal("99.00"));
        order.setTax(new BigDecimal("80.00"));
        order.setTotal(new BigDecimal("1179.00"));
        order.setShippingFullName("Integration Customer");
        order.setShippingPhone("9876543210");
        order.setShippingLine1("1 Test Rd");
        order.setShippingCity("Testville");
        order.setShippingState("TS");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("India");
        order = orderRepository.save(order);

        User other = new User();
        other.setEmail("other.customer@test.local");
        other.setFullName("Other Customer");
        other.setPassword(passwordEncoder.encode("OtherPass123"));
        other.setRole(Role.CUSTOMER);
        other.setActive(true);
        other = userRepository.save(other);
        String otherToken = jwtService.generateToken(new UserPrincipal(other));

        mockMvc.perform(get("/api/v1/orders/" + order.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void catalogIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void cartRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDashboardForbiddenForCustomerButAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").isNumber());
    }

    @Test
    void productSearchFiltersByPrice() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("q", "Integration")
                        .param("maxPrice", "1500")
                        .param("category", "test-laptops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Integration Laptop"));

        mockMvc.perform(get("/api/v1/products").param("maxPrice", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void cartCheckoutAndOrderHistoryFlow() throws Exception {
        String addBody = objectMapper.writeValueAsString(Map.of("productId", productId, "quantity", 2));
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.subtotal").value(2400.00));

        String addressBody = objectMapper.writeValueAsString(Map.of(
                "fullName", "Integration Customer",
                "phone", "5550000",
                "line1", "1 Test Rd",
                "city", "Testville",
                "state", "TS",
                "postalCode", "12345",
                "country", "Testland",
                "defaultAddress", true));

        String addressResponse = mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addressBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long addressId = objectMapper.readTree(addressResponse).get("id").asLong();

        String checkoutBody = objectMapper.writeValueAsString(Map.of(
                "addressId", addressId,
                "paymentMethod", "CASH_ON_DELIVERY"));

        mockMvc.perform(post("/api/v1/checkout")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(checkoutBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(2400.00))
                .andExpect(jsonPath("$.shippingFee").value(99.00))
                .andExpect(jsonPath("$.tax").value(192.00))
                .andExpect(jsonPath("$.total").value(2691.00))
                .andExpect(jsonPath("$.status").value("PENDING"));

        Product refreshed = productRepository.findById(productId).orElseThrow();
        assertThat(refreshed.getStock()).isEqualTo(3);

        mockMvc.perform(get("/api/v1/orders").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void duplicateReviewIsRejected() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "rating", 5, "title", "Great", "comment", "Loved it"));

        mockMvc.perform(post("/api/v1/products/" + productId + "/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products/" + productId + "/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void finderReturnsDatabaseBackedResults() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "categorySlug", "test-laptops",
                "budget", 2000,
                "purpose", "programming",
                "priorities", Map.of("performance", 5),
                "maxResults", 5));

        mockMvc.perform(post("/api/v1/finder/recommendations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].product.name").value("Integration Laptop"))
                .andExpect(jsonPath("$.results[0].withinBudget").value(true));
    }

    @Test
    void compatibilityReturnsUnknownWithoutRules() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "sourceType", "CAMERA",
                "sourceAttributes", Map.of("Mount", "E"),
                "targetType", "LENS",
                "targetAttributes", Map.of("Mount", "Z")));

        mockMvc.perform(post("/api/v1/compatibility/check")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("UNKNOWN"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void lifecycleReturnsNoContentWhenNoAssessment() throws Exception {
        mockMvc.perform(get("/api/v1/lifecycle/" + productId))
                .andExpect(status().isNoContent());
    }

    @Test
    void priceHistoryReturnsCatalogFallback() throws Exception {
        mockMvc.perform(get("/api/v1/products/" + productId + "/price-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.latestPrice").value(1200.00));
    }
}
