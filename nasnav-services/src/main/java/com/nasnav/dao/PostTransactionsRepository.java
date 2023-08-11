package com.nasnav.dao;

import com.nasnav.persistence.PostTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostTransactionsRepository extends JpaRepository<PostTransactions, Long> {
    @Query("select sum (pt.paidCoins) from PostTransactions  pt where pt.post.id= :postId")
    Long sumAllPaidCoins(@Param("postId") Long postId);
}
