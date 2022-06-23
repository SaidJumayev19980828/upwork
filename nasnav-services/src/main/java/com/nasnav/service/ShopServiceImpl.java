package com.nasnav.service;


import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.ShopIdAndPriority;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.querydsl.sql.*;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.helpers.ShopServiceHelper;
import com.nasnav.service.helpers.UserServicesHelper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
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
import java.util.Map;
import java.util.Set;

import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
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
    private AddressRepository addressRepo;

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
        ShopRepresentationObject shopRepObj = shopsRepository.findByIdAndRemoved(shopId)
                .map(shop -> (ShopRepresentationObject) shop.getRepresentation())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0002, shopId));

        List<OrganizationImagesRepresentationObject> images = orgImgRepo.findByShopsEntityIdAndTypeNot(shopId, 360)
                .stream()
                .map(entity -> (OrganizationImagesRepresentationObject) entity.getRepresentation())
                .collect(toList());
        shopRepObj.setImages(images);

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
		return shopsRepository
                .findByIdAndOrganizationEntity_IdAndRemoved(shopJson.getId(), org.getId(), 0)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0002, shopJson.getId()));
	}
    
    
    
    @Override
    public List<ShopRepresentationObject> getLocationShops(LocationShopsParam param) {
        BooleanBuilder predicate = getQueryPredicate(param, true);

        SQLQuery query = getShopsQuery(param, predicate);

        List<ShopRepresentationObject> shops = template.query(query.getSQL().getSQL(),
                new BeanPropertyRowMapper<>(ShopRepresentationObject.class));

        List<Long> addressesIds = shops.stream().map(ShopRepresentationObject::getAddressId).collect(toList());
        Map<Long, AddressRepObj> addresses = addressRepo.findByIdIn(addressesIds)
                .stream()
                .collect(toMap(AddressesEntity::getId, a -> (AddressRepObj) a.getRepresentation()));
        shops.stream().map(s -> this.setShopAddress(s, addresses)).collect(toList());
        return shops;
    }

    @Override
    public Set<CityIdAndName> getLocationShopsCities(LocationShopsParam param) {
        BooleanBuilder predicate = getQueryPredicate(param, false);

        SQLQuery query = getShopsQuery(param, predicate);

        return template.query(query.getSQL().getSQL(),
                        new BeanPropertyRowMapper<>(ShopRepresentationObject.class))
                .stream()
                .map(s -> new CityIdAndName(s.getCityId(), s.getCityName()))
                .collect(toSet());
    }

    private SQLQuery getShopsQuery(LocationShopsParam param, BooleanBuilder predicate) {
        SQLQuery productsQuery = null;
        SQLQuery collectionsQuery = null;
        SQLQuery query = null;
        if (asList(param.getProductType()).contains(0)) {
            productsQuery = getProductsOrCollectionsQuery(predicate, param.isSearchInTags(), false);
        }
        if (asList(param.getProductType()).contains(2)) {
            collectionsQuery = getProductsOrCollectionsQuery(predicate, param.isSearchInTags(), true);
        }
        if (productsQuery != null && collectionsQuery != null) {
            Expression e = new SQLQuery<>().union(productsQuery, collectionsQuery).as("total");
            query = queryFactory.select(Expressions.template(ShopRepresentationObject.class, "*"))
                    .from(e)
                    .limit(param.getCount());
        } else if (productsQuery != null) {
            query = queryFactory.select(Expressions.template(ShopRepresentationObject.class, "*"))
                    .from(productsQuery.as("total"))
                    .limit(param.getCount());
        } else if (collectionsQuery != null) {
            query = queryFactory.select(Expressions.template(ShopRepresentationObject.class, "*"))
                    .from(collectionsQuery.as("total"))
                    .limit(param.getCount());
        }
        return query;
    }

    private ShopRepresentationObject setShopAddress(ShopRepresentationObject shop, Map<Long, AddressRepObj> addresses) {
        AddressRepObj address = addresses.get(shop.getAddressId());
        if (address != null) {
            shop.setAddress(address);
        }
        return shop;
    }



    private BooleanBuilder getQueryPredicate(LocationShopsParam param, boolean addLocationConditions) {
        BooleanBuilder predicate = new BooleanBuilder();
        QShops shop = QShops.shops;
        QProductVariants variant = QProductVariants.productVariants;
        QProducts product = QProducts.products;
        QTags tag = QTags.tags;
        QOrganizations organization = QOrganizations.organizations;
        QBrands brand = QBrands.brands;

        predicate.and(shop.removed.eq(0));
        predicate.and(product.removed.eq(0));
        predicate.and(product.hide.eq(false));
        predicate.and(variant.removed.eq(0));
        if(param.getName() != null) {
            if (param.isSearchInTags()) {
                predicate.and(product.name.lower().like( "%" + param.getName().toLowerCase() + "%")
                        .or(product.description.lower().like( "% " + param.getName().toLowerCase() + " %"))
                        .or(product.description.lower().like( param.getName().toLowerCase() + " %"))
                        .or(product.description.lower().like( "% " + param.getName().toLowerCase()))
                        .or(tag.name.lower().like("%" + param.getName().toLowerCase() + "%")));
            }
            else {
                predicate.and(product.name.lower().like("%" + param.getName().toLowerCase() + "%")
                        .or(product.description.lower().like( param.getName().toLowerCase() + " %"))
                        .or(product.description.lower().like( "% " + param.getName().toLowerCase()))
                        .or(product.description.lower().like( "% " + param.getName().toLowerCase() + " %")));
            }
        }
        if (param.isYeshteryState()) {
           predicate.and(organization.yeshteryState.eq(1));
        }
        if (param.getOrgId() != null) {
            predicate.and(product.organizationId.eq(param.getOrgId()));
        }
        if(param.getBrandName() != null){
            predicate.and(brand.name.eq(param.getBrandName()));
        }
        if (param.getProductType() != null) {
            predicate.and(product.productType.in(param.getProductType()));
        } else {
            param.setProductType(new Integer[]{0});
        }
        if (addLocationConditions)
            addLocationPredicateConditions(param, predicate);

        return predicate;
    }

    private void addLocationPredicateConditions(LocationShopsParam param, BooleanBuilder predicate) {
        QAddresses address = QAddresses.addresses;
        QAreas area = QAreas.areas;
        if (!anyIsNull(param.getMinLongitude(), param.getMinLatitude(), param.getMaxLongitude(), param.getMaxLatitude())) {
            predicate.and(address.latitude.between(param.getMinLatitude(), param.getMaxLatitude()))
                    .and(address.longitude.between(param.getMinLongitude(), param.getMaxLongitude()));
        } else if (param.getAreaId() != null) {
            predicate.and(area.id.eq(param.getAreaId()));
        } else if (param.getCityId() != null) {
            predicate.and(area.id.in(queryFactory.select(area.id).from(area).where(area.cityId.eq(param.getCityId()))));
        } else if (!anyIsNull(param.getLongitude(), param.getLatitude())) {
            Double minLat, maxLat, minLong, maxLong, radius;
            radius = ofNullable(param.getRadius()).orElse(0.1);
            minLong = getMinOrMaxLongitude(param.getLongitude(), radius ,225 );
            minLat = getMinOrMaxLatitude(param.getLatitude(), radius ,225 );
            maxLong = getMinOrMaxLongitude(param.getLongitude(), radius ,45 );
            maxLat = getMinOrMaxLatitude(param.getLatitude(), radius ,45 );
            predicate.and(address.latitude.between(minLat, maxLat))
                    .and(address.longitude.between(minLong, maxLong));
        }
    }

    private double getMinOrMaxLongitude(double longitude, double radius, int angel) {
        return longitude + radius * Math.cos(angel * Math.PI / 180);
    }

    private double getMinOrMaxLatitude(double latitude, double radius, int angel) {
        return latitude + radius * Math.sin(angel * Math.PI / 180);
    }

    private SQLQuery getProductsOrCollectionsQuery(BooleanBuilder predicate, boolean searchInTags, boolean collections) {
        QShops shop = QShops.shops;
        QStocks stock = QStocks.stocks;
        QProductVariants variant = QProductVariants.productVariants;
        QAddresses address = QAddresses.addresses;
        QAreas area = QAreas.areas;
        QCities city = QCities.cities;
        QBrands brand = QBrands.brands;
        QOrganizations organizations = QOrganizations.organizations;
        QShop360s shop360 = QShop360s.shop360s;
        SQLQuery query = getShopsQuerySelectPart()
                    .from(stock)
                    .innerJoin(shop).on(stock.shopId.eq(shop.id))
                    .leftJoin(shop360).on(shop360.shopId.eq(shop.id))
                    .innerJoin(variant).on(stock.variantId.eq(variant.id))
                    .leftJoin(address).on(shop.addressId.eq(address.id))
                    .leftJoin(area).on(address.areaId.eq(area.id))
                    .innerJoin(city).on(area.cityId.eq(city.id))
                    .innerJoin(organizations).on(shop.organizationId.eq(organizations.id))
                    .innerJoin(brand).on(shop.brandId.eq(brand.id));
        if (collections) {
            query = addShopsQueryCollectionsPart(query);
        } else {
            query = addShopsQueryProductsPart(query);
        }
        if (searchInTags) {
            query = addShopsQueryTagsPart(query);
        }
        query
            .where(predicate)
            .groupBy(shop.id, shop.name, shop.pName, shop.logo, shop.darkLogo, shop.banner,
                    shop.googlePlaceId, shop.isWarehouse, shop.priority, shop.addressId,
                    city.id, city.name)
            .orderBy(shop.priority.desc(), shop.id.asc());
        return query;
    }

    private SQLQuery<Tuple> getShopsQuerySelectPart() {
        QShops shop = QShops.shops;
        QCities city = QCities.cities;
        QShop360s shop360 = QShop360s.shop360s;

        return queryFactory.select(shop.id, shop.name, shop.pName, shop.logo, shop.darkLogo, shop.banner,
                        shop.googlePlaceId, shop.isWarehouse, shop.priority, shop.addressId,
                        city.id.as("cityId"), city.name.as("cityName"), shop360.count().gt(0).as("has360"))
                .distinct();
    }

    private SQLQuery<Tuple> addShopsQueryProductsPart(SQLQuery<Tuple> query) {
        QProductVariants variant = QProductVariants.productVariants;
        QProducts product = QProducts.products;
        return query.innerJoin(product).on(variant.productId.eq(product.id));
    }
    private SQLQuery<Tuple> addShopsQueryCollectionsPart(SQLQuery<Tuple> query) {
        QProductVariants variant = QProductVariants.productVariants;
        QProductCollections collection = QProductCollections.productCollections;
        QProducts product = QProducts.products;
        return query
                .innerJoin(collection).on(collection.variantId.eq(variant.id))
                .innerJoin(product).on(collection.productId.eq(product.id));
    }

    private SQLQuery<Tuple> addShopsQueryTagsPart(SQLQuery<Tuple> query) {
        QProducts product = QProducts.products;
        QProductTags productTag = QProductTags.productTags;
        QTags tag = QTags.tags;
        return query.
                leftJoin(productTag).on(product.id.eq(productTag.productId))
                .leftJoin(tag).on(tag.id.eq(productTag.tagId));
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

    @Override
    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    public void changeShopsPriority(List<ShopIdAndPriority> dto) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        Map<Long, Integer> shopsPrioritiesMap = dto.stream().collect(toMap(ShopIdAndPriority::getShopId, ShopIdAndPriority::getPriority));
        List<ShopsEntity> entities = shopsRepository.findByIdInAndOrganizationEntity_IdAndRemoved( shopsPrioritiesMap.keySet(), orgId, 0);
        entities.stream()
                .filter(shop -> shopsPrioritiesMap.get(shop.getId()) != null)
                .forEach(shop -> shop.setPriority(shopsPrioritiesMap.get(shop.getId())));
        shopsRepository.saveAll(entities);
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
