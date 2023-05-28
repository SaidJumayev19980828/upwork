package com.nasnav.dao;

import com.nasnav.persistence.BankAccountActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountActivityRepository extends JpaRepository<BankAccountActivityEntity, Long> {
}
