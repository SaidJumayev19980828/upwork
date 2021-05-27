package com.nasnav.service;


import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationImagesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationImagesEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.querydsl.sql.*;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.helpers.ShopServiceHelper;
import com.nasnav.service.helpers.UserServicesHelper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import java.util.List;
import java.util.Optional;

import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.*;

@Service
public class ShopServiceImpl implements ShopService {
	
	@Autowired
	private SecurityService securityService;
    @Autowired
    private SQLQueryFactory queryFactory;
    @Autowired
    private JdbcTemplate template;
    private final ShopsRepository shopsRepository;
    private final UserServicesHelper userServicehelper;
    private final ShopServiceHelper shopServiceHelper;
    private final OrganizationImagesRepository orgImgRepo;

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private EmployeeUserRepository empUserRepo;
    
    @Autowired
    public ShopServiceImpl(ShopsRepository shopsRepository, UserServicesHelper userServicehelper,
                           ShopServiceHelper shopServiceHelper, OrganizationImagesRepository orgImgRepo){
        this.shopsRepository = shopsRepository;
        this.userServicehelper = userServicehelper;
        this.shopServiceHelper = shopServiceHelper;
        this.orgImgRepo = orgImgRepo;
    }
    
    
    

    
    @Override
    @CacheResult(cacheName = ORGANIZATIONS_SHOPS)
    public List<ShopRepresentationObject> getOrganizationShops(Long organizationId, boolean showWarehouses) {
    	//TODO this filtering is better to be done in the where condition of a single jpa query similar to
    	//similar to ProductRepository.find360Collections
    	//but i was in hurry
    	List<ShopsEntity> shopsEntities = emptyList();
    	if(showWarehouses) {
    		shopsEntities = shopsRepository.findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(organizationId, 0);
    	}else {
    		shopsEntities = shopsRepository.findByOrganizationEntity_IdAndRemovedAndIsWarehouseOrderByPriorityDesc(organizationId, 0, 0);
    	}

        return shopsEntities
        		.stream()
        		.map(shopsEntity -> (ShopRepresentationObject) shopsEntity.getRepresentation())
        		.collect(toList());
    }
    
    
    

    
//    @CacheResult(cacheName = "shops_by_id")
    @Override
    public ShopRepresentationObject getShopById(Long shopId) {

        Optional<ShopsEntity> shopsEntityOptional = shopsRepository.findByIdAndRemoved(shopId, 0);

        if(shopsEntityOptional==null || !shopsEntityOptional.isPresent())
            throw new RuntimeBusinessException(NOT_FOUND, S$0003);

        ShopRepresentationObject shopRepObj = (ShopRepresentationObject)shopsEntityOptional.get().getRepresentation();
        List<OrganizationImagesEntity> imageEntities = orgImgRepo.findByShopsEntityIdAndTypeNot(shopId, 360);
        if(imageEntities != null && !imageEntities.isEmpty())
            shopRepObj.setImages(imageEntities.stream().map(entity -> (OrganizationImagesRepresentationObject) entity.getRepresentation())
                                                       .collect(toList()));

        return  shopRepObj;
    }
    
    
    
    
    @Override
    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    public ShopResponse shopModification(ShopJsonDTO shopJson) {
        EmployeeUserEntity user = (EmployeeUserEntity)securityService.getCurrentUser();
        List<String> userRoles = userServicehelper.getEmployeeUserRoles(user.getId());
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        if (shopJson.getId() == null){
            return createShop(shopJson, org, userRoles);
        } else {
            return updateShop(shopJson, org);
        }
    }
    
    
    
    

    private ShopResponse createShop(ShopJsonDTO shopJson, OrganizationEntity org, List<String> userRoles) {
        if (!userRoles.contains("ORGANIZATION_MANAGER")) {
            throw new RuntimeBusinessException(FORBIDDEN, U$AUTH$0001, "this shop");
        }
        ShopsEntity shopsEntity = new ShopsEntity();

        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson, org);
        shopsEntity.setOrganizationEntity(org);
        shopsEntity.setRemoved(0);
        shopsRepository.save(shopsEntity);
        return new ShopResponse(shopsEntity.getId());
    }
    

    private ShopResponse updateShop(ShopJsonDTO shopJson, OrganizationEntity org) {
    	ShopsEntity shopsEntity = validateAndReturnShop(shopJson, org);
        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson, org);
        shopsEntity.setOrganizationEntity(org);
        shopsRepository.save(shopsEntity);
        
        return new ShopResponse(shopsEntity.getId());
    }


	private  ShopsEntity validateAndReturnShop(ShopJsonDTO shopJson, OrganizationEntity org)  {
		ShopsEntity shopsEntity = shopsRepository.findByIdAndOrganizationEntity_IdAndRemoved(shopJson.getId(), org.getId(), 0);
        if ( shopsEntity == null) {
            throw new RuntimeBusinessException(NOT_FOUND, S$0002, shopJson.getId());
        }
        return shopsEntity;
	}
    
    
    
    @Override
    public List<ShopRepresentationObject> getLocationShops(String name, Long orgId, Long areaId, Double longitude, Double latitude, Double radius) {
        QShops shop = QShops.shops;

        BooleanBuilder predicate = getQueryPredicate(name, orgId, areaId, longitude, latitude, radius);
        SQLQuery fromClaus = getFromClaus(predicate);
        SQLQuery query = (SQLQuery) fromClaus.select(shop.id, shop.name, shop.pName, shop.logo, shop.banner,
                                   shop.googlePlaceId, shop.isWarehouse, shop.priority).distinct();

        return template.query(query.getSQL().getSQL(),
                new BeanPropertyRowMapper<>(ShopRepresentationObject.class));

    }

    private BooleanBuilder getQueryPredicate(String name, Long orgId, Long areaId, Double longitude, Double latitude, Double radius) {
        BooleanBuilder predicate = new BooleanBuilder();
        QShops shop = QShops.shops;
        QProducts product = QProducts.products;
        QTags tag = QTags.tags;
        QAddresses address = QAddresses.addresses;
        QAreas area = QAreas.areas;

        predicate.and(shop.removed.eq(0));
        if(name != null) {
            predicate.and(product.name.likeIgnoreCase(name)
                        .or(tag.name.likeIgnoreCase(name)));
        }
        if(orgId != null) {
            predicate.and(product.organizationId.eq(orgId));
        }
        if (areaId != null) {
            predicate.and(area.id.eq(areaId));
        }
        if (longitude != null && latitude != null) {
            Double minLat, maxLat, minLong, maxLong;
            minLong = longitude + radius * Math.cos(225 * Math.PI / 180);
            minLat = latitude + radius * Math.sin(225 * Math.PI / 180);
            maxLong = longitude + radius * Math.cos(45 * Math.PI / 180);
            maxLat = latitude + radius * Math.sin(45 * Math.PI / 180);
            predicate.and(address.latitude.between(minLat, maxLat))
                    .and(address.longitude.between(minLong, maxLong));
        }

        return predicate;
    }

    private SQLQuery<?> getFromClaus(BooleanBuilder predicate) {
        QShops shop = QShops.shops;
        QStocks stock = QStocks.stocks;
        QProductVariants variant = QProductVariants.productVariants;
        QProducts product = QProducts.products;
        QProductTags productTag = QProductTags.productTags;
        QTags tag = QTags.tags;
        QAddresses address = QAddresses.addresses;
        QAreas area = QAreas.areas;

        return queryFactory.from(stock)
                .innerJoin(shop).on(stock.shopId.eq(shop.id))
                .innerJoin(variant).on(stock.variantId.eq(variant.id))
                .innerJoin(product).on(variant.productId.eq(product.id))
                .leftJoin(productTag).on(product.id.eq(productTag.productId))
                .leftJoin(tag).on(tag.id.eq(productTag.tagId))
                .leftJoin(address).on(shop.addressId.eq(address.id))
                .leftJoin(area).on(address.areaId.eq(area.id))
                .where(predicate);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    public void deleteShop(Long shopId)  {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (!shopsRepository.existsByIdAndOrganizationEntity_IdAndRemoved(shopId, orgId, 0)) {
            throw new RuntimeBusinessException(NOT_FOUND, S$0002, shopId);
        }

        validateShopLinksBeforeDelete(shopId);

        stockRepo.setStocksQuantityZero(shopId);

        shopsRepository.setShopHidden(shopId);
    }


    private void validateShopLinksBeforeDelete(Long shopId) {
        List<Long> linkedEmployees = empUserRepo.findByShopId(shopId)
                                                .stream()
                                                .map(EmployeeUserEntity::getId)
                                                .collect(toList());
        if (!linkedEmployees.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0001, "employees "+linkedEmployees.toString());
        }
    }
	
}
