package com.ecommerce.seller.repository;

import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.model.SellerApprovalStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository
        extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUserId(
            Long userId
    );

    boolean existsByUserId(
            Long userId
    );

    List<Seller> findByApprovalStatus(
            SellerApprovalStatus approvalStatus
    );
}