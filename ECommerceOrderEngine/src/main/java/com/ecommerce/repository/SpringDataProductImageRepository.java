
package com.ecommerce.repository;

import com.ecommerce.model.ProductImage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct_IdOrderBySortOrderAscIdAsc(
            long productId
    );

    Optional<ProductImage> findByIdAndProduct_Id(
            long imageId,
            long productId
    );

    Optional<ProductImage> findByProduct_IdAndPrimaryTrue(
            long productId
    );

    boolean existsByProduct_Id(
            long productId
    );

    long countByProduct_Id(
            long productId
    );
}
