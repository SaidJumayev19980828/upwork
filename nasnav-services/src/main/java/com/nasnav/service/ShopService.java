package com.nasnav.service;

import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.response.ShopResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import java.util.List;

import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;

public interface ShopService {
    @CacheResult(cacheName = ORGANIZATIONS_SHOPS)
    List<ShopRepresentationObject> getOrganizationShops(Long organizationId, boolean showWarehouses);

    //    @CacheResult(cacheName = "shops_by_id")
    ShopRepresentationObject getShopById(Long shopId);

    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    ShopResponse shopModification(ShopJsonDTO shopJson);

    List<ShopRepresentationObject> getLocationShops(String name, Long orgId);

    @Transactional
    @CacheEvict(cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    void deleteShop(Long shopId);
}
