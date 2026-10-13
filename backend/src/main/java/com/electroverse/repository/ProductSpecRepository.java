package com.electroverse.repository;

import com.electroverse.entity.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    List<ProductSpec> findByProductIdOrderBySpecGroupAscIdAsc(Long productId);

    List<ProductSpec> findByProductIdIn(List<Long> productIds);
}
