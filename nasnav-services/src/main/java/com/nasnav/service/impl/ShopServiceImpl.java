package com.nasnav.service.impl;


import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.ShopIdAndPriority;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.querydsl.sql.*;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.RoleService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ShopService;
import com.nasnav.service.helpers.ShopServiceHelper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Arrays.asList;
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
    @Autowired
    ShopRatingRepository shopRatingRepository;
    private final ShopsRepository shopsRepository;
    private final ShopServiceHelper shopServiceHelper;
    private final OrganizationImagesRepository orgImgRepo;
    private final RoleService roleService;

    @Autowired
    private StockRepository stockRepo;
    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private EmployeeUserRepository empUserRepo;
    
    @Autowired
    public ShopServiceImpl(ShopsRepository shopsRepository, ShopServiceHelper shopServiceHelper,
                           OrganizationImagesRepository orgImgRepo, RoleService roleService){
        this.shopsRepository = shopsRepository;
        this.shopServiceHelper = shopServiceHelper;
        this.orgImgRepo = orgImgRepo;
        this.roleService = roleService;
    }
    
    
    

    
    @Override
    @CacheResult(cacheName = ORGANIZATIONS_SHOPS)
    public PageImpl<ShopRepresentationObject> getOrganizationShops(Long organizationId, boolean showWarehouses,Integer start, Integer count) {
    	//TODO this filtering is better to be done in the where condition of a single jpa query similar to
    	//similar to ProductRepository.find360Collections
    	//but i was in hurry
        Pageable page = new CustomPaginationPageRequest(start, count);

        PageImpl<ShopsEntity> shopsEntities ;
    	if(showWarehouses) {
    		shopsEntities = shopsRepository.findPageableByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(organizationId, 0,page);
    	}else {
    		shopsEntities = shopsRepository.findByOrganizationEntity_IdAndRemovedAndIsWarehouseOrderByPriorityDesc(organizationId, 0, 0,page);
    	}

        List<ShopRepresentationObject> dtos = shopsEntities.getContent().stream().map(shopsEntity -> (ShopRepresentationObject) shopsEntity.getRepresentation()).collect(Collectors.toList());
        return new PageImpl<>(dtos, shopsEntities.getPageable(), shopsEntities.getTotalElements());
    }
    
    
    

    
//    @CacheResult(cacheName = "shops_by_id")
    @Override
    public ShopRepresentationObject getShopById(Long shopId) {
        ShopRepresentationObject shopRepObj = shopById(shopId).getRepresentation();
        List<OrganizationImagesRepresentationObject> images = orgImgRepo.findByShopsEntityIdAndTypeNot(shopId, 360)
                .stream()
                .map(entity -> (OrganizationImagesRepresentationObject) entity.getRepresentation())
                .collect(toList());
        shopRepObj.setImages(images);

        return  shopRepObj;
    }

    @Override
    public ShopsEntity shopById(Long shopId) {
        return shopsRepository.findByIdAndRemoved(shopId).orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0002, shopId));
    }


    @Override
    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    public ShopResponse shopModification(ShopJsonDTO shopJson) {
        EmployeeUserEntity user = (EmployeeUserEntity)securityService.getCurrentUser();
        List<String> userRoles = roleService.getRolesNamesOfEmployeeUser(user.getId());
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
                        .or(tag.name.lower().like("%" + param.getName().toLowerCase() + "%"))
                        .or(brand.name.lower().like( "%" + param.getName().toLowerCase() + "%")));
            }
            else {
                predicate.and(product.name.lower().like( "%" + param.getName().toLowerCase() + "%")
                        .or(product.description.lower().like( "% " + param.getName().toLowerCase() + " %"))
                        .or(product.description.lower().like( param.getName().toLowerCase() + " %"))
                        .or(product.description.lower().like( "% " + param.getName().toLowerCase()))
                        .or(brand.name.lower().like( "%" + param.getName().toLowerCase() + "%")));
            }
        }
        if (param.isYeshteryState()) {
           predicate.and(organization.yeshteryState.eq(1));
        }
        if (param.getOrgId() != null) {
            predicate.and(product.organizationId.eq(param.getOrgId()));
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
                    .innerJoin(organizations).on(shop.organizationId.eq(organizations.id));

        if (collections) {
            query = addShopsQueryCollectionsPart(query);
        } else {
            query = addShopsQueryProductsPart(query);
        }

        query = addShopsQueryBrandsPart(query);

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

    private SQLQuery<Tuple> addShopsQueryBrandsPart(SQLQuery<Tuple> query) {
        QProducts product = QProducts.products;
        QBrands brand = QBrands.brands;
        return query.innerJoin(brand).on(brand.id.eq(product.brandId));
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

    @Override
    public List<ShopRepresentationObject> getLocationShops(String name, Long orgId, Long areaId, Long cityId,
            Double minLongitude, Double minLatitude, Double maxLongitude, Double maxLatitude, Double longitude,
            Double latitude, Double radius, boolean yeshteryState, boolean searchInTags, Integer[] productType,
            Long count) {
        LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, cityId, minLongitude, minLatitude,
                maxLongitude, maxLatitude,
                longitude, latitude, radius, false, searchInTags, productType, count);
        return getLocationShops(param);
    }

    @Override
    public Set<CityIdAndName> getLocationShopsCities(String name, Long orgId, Long areaId, Long cityId,
            Double minLongitude, Double minLatitude, Double maxLongitude, Double maxLatitude, Double longitude,
            Double latitude, Double radius, boolean yeshteryState, boolean searchInTags, Integer[] productType,
            Long count) {
        LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, cityId, minLongitude, minLatitude,
                maxLongitude, maxLatitude,
                longitude, latitude, radius, false, searchInTags, productType, count);
        return getLocationShopsCities(param);
    }

    public ShopRateDTO rateShop(ShopRateDTO dto) {
        BaseUserEntity baseUser = securityService.getCurrentUser();
        if (baseUser instanceof EmployeeUserEntity) {
            throw new RuntimeBusinessException(FORBIDDEN, E$USR$0001);
        }
        UserEntity user = (UserEntity) baseUser;
        ShopsEntity shopsEntity = shopsRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$VAR$0001, dto.getShopId()));
       return createShopRate(dto, shopsEntity, user);
    }


    private ShopRateDTO createShopRate(ShopRateDTO dto, ShopsEntity shop, UserEntity user) {
        ShopRating rate = shopRatingRepository.findByShopIdAndUserId(shop.getId(), user.getId())
                .orElse(new ShopRating());
        rate.setRate(dto.getRate());
        rate.setShop(shop);
        rate.setReview(dto.getReview());
        rate.setUser(user);
        rate.setApproved(false);
        shopRatingRepository.save(rate);
        return dto;
    }

}
