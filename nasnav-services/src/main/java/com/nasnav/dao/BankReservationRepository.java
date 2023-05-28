package com.nasnav.dao;

import com.nasnav.persistence.BankReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankReservationRepository extends JpaRepository<BankReservationEntity, Long> {
}
