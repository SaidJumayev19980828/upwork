package com.nasnav.dao;

import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralWalletRepository extends JpaRepository<ReferralWallet, Long> {

    Optional<ReferralWallet> findByUserId(UserEntity userId);
    PageImpl<ReferralWallet> findAll(Pageable page);
}