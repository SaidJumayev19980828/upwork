package com.nasnav.dao;

import com.nasnav.persistence.GiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface GiftRepository extends JpaRepository<GiftEntity, Long> {

    List<GiftEntity> getByUserFrom_Id(Long userId);
    List<GiftEntity> getByUserFrom_IdAndIsRedeemFalse(Long userId);
    List<GiftEntity> getByUserTo_IdAndIsRedeemTrue(Long userId);

    Optional<GiftEntity> findByEmail(String email);

    @Transactional
    @Modifying
    void deleteByEmail(String email);
}
