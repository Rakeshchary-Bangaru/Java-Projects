package com.ecommerce.repository;

import com.ecommerce.model.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataInventoryRepository
        extends JpaRepository<Inventory, Long> {

    @Query("""
            select i.quantity
            from Inventory i
            where i.productId = :productId
            """)
    Optional<Integer> findStockByProductId(
            @Param("productId") long productId
    );


    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            update Inventory i
            set i.quantity = i.quantity - :quantity
            where i.productId = :productId
              and i.quantity >= :quantity
            """)
    int reserveStock(
            @Param("productId") long productId,
            @Param("quantity") int quantity
    );


    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            update Inventory i
            set i.quantity = i.quantity + :quantity
            where i.productId = :productId
            """)
    int releaseStock(
            @Param("productId") long productId,
            @Param("quantity") int quantity
    );


    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query(
            value = """
                    INSERT INTO inventory
                        (product_id, quantity)
                    VALUES
                        (:productId, :quantity)
                    ON DUPLICATE KEY UPDATE
                        quantity = quantity + :quantity
                    """,
            nativeQuery = true
    )
    int addStock(
            @Param("productId") long productId,
            @Param("quantity") int quantity
    );


    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query(
            value = """
                    INSERT INTO inventory
                        (product_id, quantity)
                    VALUES
                        (:productId, :quantity)
                    ON DUPLICATE KEY UPDATE
                        quantity = :quantity
                    """,
            nativeQuery = true
    )
    int setStock(
            @Param("productId") long productId,
            @Param("quantity") int quantity
    );
}