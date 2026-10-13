package com.electroverse.service;

import com.electroverse.dto.request.CatalogRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.entity.Category;
import com.electroverse.entity.Product;
import com.electroverse.entity.ProductImage;
import com.electroverse.entity.ProductSpec;
import com.electroverse.exception.ConflictException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.CategoryRepository;
import com.electroverse.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public CatalogResponses.Paged<CatalogResponses.ProductSummary> search(
            String q, String category, String brand, BigDecimal minPrice, BigDecimal maxPrice,
            Double minRating, Boolean inStock, Boolean featured, Map<String, String> specs,
            String sortBy, int page, int size, boolean includeInactive) {

        ProductSpecification.ProductQuery query = new ProductSpecification.ProductQuery(
                q, category, brand, minPrice, maxPrice, minRating, inStock, featured, includeInactive, specs);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 60), resolveSort(sortBy));
        Page<Product> results = productRepository.findAll(ProductSpecification.build(query), pageable);
        return DtoMapper.toPaged(results, DtoMapper::toProductSummary);
    }

    @Transactional(readOnly = true)
    public CatalogResponses.ProductDetail getDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        return DtoMapper.toProductDetail(product);
    }

    @Transactional(readOnly = true)
    public CatalogResponses.ProductDetail getDetailBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found: " + slug);
        }
        return DtoMapper.toProductDetail(product);
    }

    @Transactional(readOnly = true)
    public CatalogResponses.ProductDetail getAdminDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return DtoMapper.toProductDetail(product);
    }

    @Transactional(readOnly = true)
    public Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<CatalogResponses.Spec> getSpecifications(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return product.getSpecs().stream().map(DtoMapper::toSpec).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogResponses.ProductSummary> featured() {
        return productRepository.findTop8ByFeaturedTrueAndActiveTrueOrderByPopularityDesc()
                .stream().map(DtoMapper::toProductSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogResponses.ProductSummary> newArrivals() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc()
                .stream().map(DtoMapper::toProductSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogResponses.ProductSummary> deals() {
        return productRepository.findTop8ByActiveTrueAndCompareAtPriceIsNotNullOrderByPopularityDesc()
                .stream().map(DtoMapper::toProductSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogResponses.ProductSummary> related(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        ProductSpecification.ProductQuery query = new ProductSpecification.ProductQuery(
                null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                null, null, null, null, null, null, false, null);
        return productRepository.findAll(ProductSpecification.build(query),
                        PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "popularity")))
                .getContent().stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .map(DtoMapper::toProductSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> brands() {
        return productRepository.findDistinctBrands();
    }

    @Transactional
    public CatalogResponses.ProductDetail create(CatalogRequests.Product request) {
        if (request.slug() != null && !request.slug().isBlank() && productRepository.existsBySlug(CategoryService.toSlug(request.slug()))) {
            throw new ConflictException("A product with this slug already exists");
        }
        Product product = new Product();
        apply(product, request);
        return DtoMapper.toProductDetail(productRepository.save(product));
    }

    @Transactional
    public CatalogResponses.ProductDetail update(Long id, CatalogRequests.Product request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        apply(product, request);
        return DtoMapper.toProductDetail(productRepository.save(product));
    }

    @Transactional
    public CatalogResponses.ProductDetail setActive(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(active);
        return DtoMapper.toProductDetail(productRepository.save(product));
    }

    @Transactional
    public CatalogResponses.ProductDetail updateStock(Long id, int stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setStock(stock);
        return DtoMapper.toProductDetail(productRepository.save(product));
    }

    private void apply(Product product, CatalogRequests.Product request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
        product.setName(request.name().trim());
        String slug = (request.slug() != null && !request.slug().isBlank())
                ? CategoryService.toSlug(request.slug())
                : CategoryService.toSlug(request.name());
        product.setSlug(slug);
        product.setBrand(request.brand());
        product.setCategory(category);
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCompareAtPrice(request.compareAtPrice());
        product.setCurrency(request.currency() != null && !request.currency().isBlank() ? request.currency() : "INR");
        product.setStock(request.stock() != null ? request.stock() : 0);
        product.setWarrantyMonths(request.warrantyMonths());
        product.setReleaseDate(request.releaseDate());
        product.setFeatured(Boolean.TRUE.equals(request.featured()));
        product.setActive(request.active() == null || request.active());

        product.getSpecs().clear();
        if (request.specs() != null) {
            int order = 0;
            for (CatalogRequests.Spec specDto : request.specs()) {
                ProductSpec spec = new ProductSpec();
                spec.setProduct(product);
                spec.setSpecGroup(specDto.specGroup());
                spec.setSpecKey(specDto.specKey());
                spec.setSpecValue(specDto.specValue());
                spec.setNumericValue(specDto.numericValue());
                spec.setSortOrder(specDto.sortOrder() != null ? specDto.sortOrder() : order++);
                product.getSpecs().add(spec);
            }
        }

        product.getImages().clear();
        List<String> imageUrls = request.images() != null ? request.images() : new ArrayList<>();
        if (request.primaryImageUrl() != null && !request.primaryImageUrl().isBlank()
                && !imageUrls.contains(request.primaryImageUrl())) {
            imageUrls.add(0, request.primaryImageUrl());
        }
        int imgOrder = 0;
        for (String url : imageUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setUrl(url);
            image.setAltText(product.getName());
            image.setSortOrder(imgOrder++);
            product.getImages().add(image);
        }
        product.setPrimaryImageUrl(imageUrls.isEmpty() ? request.primaryImageUrl() : imageUrls.get(0));
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "popularity");
        }
        return switch (sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating" -> Sort.by(Sort.Direction.DESC, "ratingAverage");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "popularity");
        };
    }
}
