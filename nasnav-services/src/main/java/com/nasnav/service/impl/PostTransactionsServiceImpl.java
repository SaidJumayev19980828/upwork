package com.nasnav.service.impl;

import com.nasnav.dao.PostRepository;
import com.nasnav.dao.PostTransactionsRepository;
import com.nasnav.persistence.PostTransactions;
import com.nasnav.service.PostTransactionsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PostTransactionsServiceImpl implements PostTransactionsService {
    private final PostTransactionsRepository postTransactionsRepository;
    private final PostRepository postRepository;

    @Transactional
    @Override
    public void saveTransactionsCoins(Long postId, Long coins) {
        PostTransactions transactions = new PostTransactions();
        transactions.setPaidCoins(coins);
        transactions.setPost(postRepository.getReferenceById(postId));
        transactions.setTransactionDate(LocalDateTime.now());
        postTransactionsRepository.save(transactions);
    }

    @Override
    public Long getPaidCoins(Long postId) {
        return Optional.ofNullable(postTransactionsRepository.sumAllPaidCoins(postId)).orElse(0L);
    }
}
