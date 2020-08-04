package com.nasnav.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import static com.nasnav.cache.Caches.*;

@Service
public class AdminService {

    @CacheEvict(cacheNames = {SHOPS_BY_ID, ORGANIZATIONS_TAGS, ORGANIZATIONS_TAG_TREES, ORGANIZATIONS_CATEGORIES, ORGANIZATIONS_EXTRA_ATTRIBUTES,
                              ORGANIZATIONS_SHOPS, ORGANIZATIONS_DOMAINS, ORGANIZATIONS_BY_ID, BRANDS, FILES, USERS_BY_TOKENS})
    public void invalidateCaches() {}
}
