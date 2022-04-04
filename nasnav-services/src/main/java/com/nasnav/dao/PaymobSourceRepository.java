package com.nasnav.dao;

import com.nasnav.persistence.PaymobSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymobSourceRepository extends JpaRepository<PaymobSourceEntity, Long> {

    List<PaymobSourceEntity> findByOrganization_Id(Long organizationId);

}
