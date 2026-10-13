package com.electroverse.service;

import com.electroverse.dto.request.CatalogRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.entity.Category;
import com.electroverse.exception.ConflictException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.CategoryRepository;
import com.electroverse.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogResponses.Category> listAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(c -> DtoMapper.toCategory(c, productRepository.countByCategoryId(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Category getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
    }

    @Transactional
    public CatalogResponses.Category create(CatalogRequests.Category request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("A category with this name already exists");
        }
        Category category = new Category();
        category.setName(request.name().trim());
        category.setSlug(resolveSlug(request.slug(), request.name()));
        category.setDescription(request.description());
        category.setIcon(request.icon());
        Category saved = categoryRepository.save(category);
        return DtoMapper.toCategory(saved, 0);
    }

    @Transactional
    public CatalogResponses.Category update(Long id, CatalogRequests.Category request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        category.setName(request.name().trim());
        if (request.slug() != null && !request.slug().isBlank()) {
            category.setSlug(toSlug(request.slug()));
        }
        category.setDescription(request.description());
        category.setIcon(request.icon());
        Category saved = categoryRepository.save(category);
        return DtoMapper.toCategory(saved, productRepository.countByCategoryId(saved.getId()));
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        if (productRepository.countByCategoryId(id) > 0) {
            throw new ConflictException("Cannot delete a category that still has products");
        }
        categoryRepository.delete(category);
    }

    private String resolveSlug(String provided, String name) {
        String base = (provided != null && !provided.isBlank()) ? provided : name;
        String slug = toSlug(base);
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }
        return slug;
    }

    public static String toSlug(String input) {
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "").replaceAll("-+", "-");
    }
}
