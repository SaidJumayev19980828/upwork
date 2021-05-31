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
import com.nasnav.request.LocationShopsParam;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.helpers.ShopServiceHelper;
import com.nasnav.service.helpers.UserServicesHelper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
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
    public List<ShopRepresentationObject> getLocationShops(LocationShopsParam param) {

        BooleanBuilder predicate = getQueryPredicate(param);
        SubQueryExpression productsQuery = getProductsQuery(predicate);
        SubQueryExpression collectionsQuery = getCollectionsQuery(predicate);
        SQLQuery query = queryFactory.select((Expressions.template(ShopRepresentationObject.class,"*")))
                .from(new SQLQuery<>().union( productsQuery,collectionsQuery).as("total"));

        return template.query(query.getSQL().getSQL(),
                new BeanPropertyRowMapper<>(ShopRepresentationObject.class));

    }

    private BooleanBuilder getQueryPredicate(LocationShopsParam param) {
        BooleanBuilder predicate = new BooleanBuilder();
        QShops shop = QShops.shops;
        QProducts product = QProducts.products;
        QTags tag = QTags.tags;
        QAddresses address = QAddresses.addresses;
        QAreas area = QAreas.areas;
        QOrganizations organization = QOrganizations.organizations;

        predicate.and(shop.removed.eq(0));
        if(param.getName() != null) {
            if (param.isSearchInTags()) {
                predicate.and(product.name.likeIgnoreCase(param.getName())
                        .or(tag.name.likeIgnoreCase(param.getName())));
            }
            else {
                predicate.and(product.name.likeIgnoreCase(param.getName()));
            }
        }
        if (param.isYeshteryState()) {
           predicate.and(organization.yeshteryState.eq(1));
        }
        if (param.getOrgId() != null) {
            predicate.and(product.organizationId.eq(param.getOrgId()));
        }
        if (param.getAreaId() != null) {
            predicate.and(area.id.eq(param.getAreaId()));
        }
        if (param.getProductType() != null) {
            predicate.and(product.productType.in(param.getProductType()));
        }
        if (param.getLongitude() != null && param.getLatitude() != null) {
            Double minLat, maxLat, minLong, maxLong, radius;
            radius = ofNullable(param.getRadius()).map(r -> r/100).orElse(0.1);
            minLong = getMinOrMaxLongitude(param.getLongitude(), radius ,225 );
            minLat = getMinOrMaxLatitude(param.getLatitude(), radius ,225 );
            maxLong = getMinOrMaxLongitude(param.getLongitude(), radius ,45 );
            maxLat = getMinOrMaxLatitude(param.getLatitude(), radius ,45 );
            predicate.and(address.latitude.between(minLat, maxLat))
                    .and(address.longitude.between(minLong, maxLong));
        }

        return predicate;
    }

    private double getMinOrMaxLongitude(double longitude, double radius, int angel) {
        return longitude + radius * Math.cos(angel * Math.PI / 180);
    }

    private double getMinOrMaxLatitude(double latitude, double radius, int angel) {
        return latitude + radius * Math.sin(angel * Math.PI / 180);
    }

    private SubQueryExpression getProductsQuery(BooleanBuilder predicate) {
        QShops shop = QShops.shops;
        QStocks stock = QStocks.stocks;
        QProductVariants variant = QProductVariants.productVariants;
        QProducts product = QProducts.products;
        QProductTags productTag = QProductTags.productTags;
        QTags tag = QTags.tags;
        QAddresses address = QAddresses.addresses;
        QAreas area = QAreas.areas;
        QOrganizations organizations = QOrganizations.organizations;

        SQLQuery productsQuery = queryFactory.select(shop.id, shop.name, shop.pName, shop.logo, shop.darkLogo, shop.banner,
                shop.googlePlaceId, shop.isWarehouse, shop.priority, address.latitude, address.longitude)
                .distinct()
                .from(stock)
                .innerJoin(shop).on(stock.shopId.eq(shop.id))
                .innerJoin(variant).on(stock.variantId.eq(variant.id))
                .innerJoin(product).on(variant.productId.eq(product.id))
                .leftJoin(productTag).on(product.id.eq(productTag.productId))
                .leftJoin(tag).on(tag.id.eq(productTag.tagId))
                .leftJoin(address).on(shop.addressId.eq(address.id))
                .leftJoin(area).on(address.areaId.eq(area.id))
                .leftJoin(organizations).on(shop.organizationId.eq(organizations.id))
                .where(predicate);
        return productsQuery;
    }

    private SubQueryExpression getCollectionsQuery(BooleanBuilder predicate) {
        QShops shop = QShops.shops;
        QStocks stock = QStocks.stocks;
        QProductVariants variant = QProductVariants.productVariants;
        QProductCollections collection = QProductCollections.productCollections;
        QProducts product = QProducts.products;
        QProductTags productTag = QProductTags.productTags;
        QTags tag = QTags.tags;
        QAddresses address = QAddresses.addresses;
        QAreas area = QAreas.areas;
        QOrganizations organizations = QOrganizations.organizations;
        SQLQuery collectionsQuery = queryFactory.select(shop.id, shop.name, shop.pName, shop.logo, shop.darkLogo, shop.banner,
                        shop.googlePlaceId, shop.isWarehouse, shop.priority, address.latitude, address.longitude)
                .distinct()
                .from(stock)
                .innerJoin(shop).on(stock.shopId.eq(shop.id))
                .innerJoin(variant).on(stock.variantId.eq(variant.id))
                .innerJoin(collection).on(collection.variantId.eq(variant.id))
                .innerJoin(product).on(collection.productId.eq(product.id))
                .leftJoin(productTag).on(product.id.eq(productTag.productId))
                .leftJoin(tag).on(tag.id.eq(productTag.tagId))
                .leftJoin(address).on(shop.addressId.eq(address.id))
                .leftJoin(area).on(address.areaId.eq(area.id))
                .leftJoin(organizations).on(shop.organizationId.eq(organizations.id))
                .where(predicate);
        return collectionsQuery;
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
