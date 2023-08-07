package com.nasnav.dao;

import com.nasnav.persistence.BankAccountActivityEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountActivityRepository extends CrudRepository<BankAccountActivityEntity, Long> {
    @Query("SELECT SUM(m.amountIn) - SUM(m.amountOut) FROM BankAccountActivityEntity m where m.account.id = :accountId and m.id > :startId")
    Long getBalance(long startId, long accountId);
    BankAccountActivityEntity findFirstByAccount_IdOrderByIdDesc(Long accountId);
    PageImpl<BankAccountActivityEntity> findAllByAccount_Id(long accountId, Pageable page);
}
