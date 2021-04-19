package com.nasnav.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


@Service
public class AdminService {

    @Autowired
    private CacheManager cacheManager;

    public void invalidateCaches() {
        cacheManager.getCacheNames()
                    .stream()
                    .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }
}
