package com.nasnav.dao;

import com.nasnav.persistence.PaymobSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymobSourceRepository extends JpaRepository<PaymobSourceEntity, Long> {

    List<PaymobSourceEntity> findByOrganization_Id(Long organizationId);

    Optional<PaymobSourceEntity> findByValue(String value);

    Optional<PaymobSourceEntity> findByNameAndOrganization_Id(String name, Long orgId);
}
