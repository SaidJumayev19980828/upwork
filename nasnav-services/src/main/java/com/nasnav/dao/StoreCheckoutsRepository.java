package com.nasnav.dao;

import com.nasnav.persistence.StoreCheckoutsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreCheckoutsRepository extends JpaRepository<StoreCheckoutsEntity, Long> {

    Optional<StoreCheckoutsEntity> findByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}
