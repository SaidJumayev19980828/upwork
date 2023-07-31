package com.nasnav.service;

public interface PostTransactionsService {
    void saveTransactionsCoins(Long postId, Long coins);

    Long getPaidCoins(Long postId);
}
