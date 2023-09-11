package com.nasnav.service.impl;

import com.nasnav.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;



@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private CacheManager cacheManager;
    @Override
    public void invalidateCaches() {
        cacheManager.getCacheNames()
                    .stream()
                    .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }
}
