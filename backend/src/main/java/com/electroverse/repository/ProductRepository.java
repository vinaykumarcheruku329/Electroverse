package com.electroverse.repository;

import com.electroverse.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    List<Product> findTop8ByFeaturedTrueAndActiveTrueOrderByPopularityDesc();

    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();

    List<Product> findTop8ByActiveTrueAndCompareAtPriceIsNotNullOrderByPopularityDesc();

    Page<Product> findByCategorySlugAndActiveTrue(String slug, Pageable pageable);

    long countByStockLessThanEqual(int threshold);

    long countByCategoryId(Long categoryId);

    @Query("select coalesce(avg(p.price),0) from Product p where p.active = true")
    double averageActivePrice();

    @Query("select distinct p.brand from Product p where p.brand is not null order by p.brand")
    List<String> findDistinctBrands();
}
