package com.nasnav.dao;

import com.nasnav.persistence.BankReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankReservationRepository extends JpaRepository<BankReservationEntity, Long> {
    @Query("SELECT SUM(m.amount) FROM BankReservationEntity m where m.account.id = :accountId and m.fulfilled = false")
    Float getReservedBalance(long accountId);
    List<BankReservationEntity> getAllByAccount_Id(long accountId);
    List<BankReservationEntity> getAllByAccount_IdAndFulfilled(long accountId, boolean isFulfilled);
}
