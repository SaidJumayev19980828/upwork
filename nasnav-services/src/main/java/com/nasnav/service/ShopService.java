package com.nasnav.service;

import com.nasnav.dto.CityIdAndName;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRateDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.request.ShopIdAndPriority;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.response.ShopResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import java.util.List;
import java.util.Set;

import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;

public interface ShopService {
    @CacheResult(cacheName = ORGANIZATIONS_SHOPS)
    PageImpl<ShopRepresentationObject> getOrganizationShops(Long organizationId, boolean showWarehouses, Integer start, Integer count);

    //    @CacheResult(cacheName = "shops_by_id")
    ShopRepresentationObject getShopById(Long shopId);
    ShopsEntity shopById(Long shopId);


    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    ShopResponse shopModification(ShopJsonDTO shopJson);

    List<ShopRepresentationObject> getLocationShops(String name,
            Long orgId,
            Long areaId,
            Long cityId,
            Double minLongitude,
            Double minLatitude,
            Double maxLongitude,
            Double maxLatitude,
            Double longitude,
            Double latitude,
            Double radius,
            boolean yeshteryState,
            boolean searchInTags,
            Integer[] productType,
            Long count);
    List<ShopRepresentationObject> getLocationShops(LocationShopsParam param);

    Set<CityIdAndName> getLocationShopsCities(String name,
            Long orgId,
            Long areaId,
            Long cityId,
            Double minLongitude,
            Double minLatitude,
            Double maxLongitude,
            Double maxLatitude,
            Double longitude,
            Double latitude,
            Double radius,
            boolean yeshteryState,
            boolean searchInTags,
            Integer[] productType,
            Long count);

    Set<CityIdAndName> getLocationShopsCities(LocationShopsParam param);

    @Transactional
    @CacheEvict(cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    void deleteShop(Long shopId);

    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    void changeShopsPriority(List<ShopIdAndPriority> dto);

     ShopRateDTO rateShop(ShopRateDTO dto);
}
