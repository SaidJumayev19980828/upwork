package com.nasnav.service;


import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;
import static com.nasnav.exceptions.ErrorCodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.cache.annotation.CacheResult;

import com.nasnav.dao.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import com.nasnav.service.helpers.ShopServiceHelper;

@Service
public class ShopService {
	
	@Autowired
	private SecurityService securityService;

    private final ShopsRepository shopsRepository;
    private final EmployeeUserServiceHelper employeeUserServicehelper;
    private final ShopServiceHelper shopServiceHelper;
    private final OrganizationImagesRepository orgImgRepo;

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private OrdersRepository orderRepo;

    @Autowired
    private ShopThreeSixtyRepository shopThreeSixtyRepo;

    @Autowired
    private EmployeeUserRepository empUserRepo;
    
    @Autowired
    public ShopService(ShopsRepository shopsRepository, EmployeeUserServiceHelper employeeUserServicehelper,
                       ShopServiceHelper shopServiceHelper,OrganizationImagesRepository orgImgRepo){
        this.shopsRepository = shopsRepository;
        this.employeeUserServicehelper = employeeUserServicehelper;
        this.shopServiceHelper = shopServiceHelper;
        this.orgImgRepo = orgImgRepo;
    }
    
    
    

    
    @CacheResult(cacheName = ORGANIZATIONS_SHOPS)
    public List<ShopRepresentationObject> getOrganizationShops(Long organizationId) {

        List<ShopsEntity> shopsEntities = shopsRepository.findByOrganizationEntity_Id(organizationId);

        if(shopsEntities==null || shopsEntities.isEmpty())
            throw new RuntimeBusinessException(NOT_FOUND, S$0003);

        return shopsEntities.stream().map(shopsEntity -> {
            ShopRepresentationObject shopRepresentationObject = ((ShopRepresentationObject) shopsEntity.getRepresentation());
            //TODO why working days won't be returned from the API unlike getShopById API
            shopRepresentationObject.setOpenWorkingDays(null);
            return shopRepresentationObject;
        }).collect(toList());
    }
    
    
    

    
//    @CacheResult(cacheName = "shops_by_id")
    public ShopRepresentationObject getShopById(Long shopId) {

        Optional<ShopsEntity> shopsEntityOptional = shopsRepository.findById(shopId);

        if(shopsEntityOptional==null || !shopsEntityOptional.isPresent())
            throw new RuntimeBusinessException(NOT_FOUND, S$0003);

        ShopRepresentationObject shopRepObj = (ShopRepresentationObject)shopsEntityOptional.get().getRepresentation();
        List<OrganizationImagesEntity> imageEntities = orgImgRepo.findByShopsEntityId(shopId);
        if(imageEntities != null && !imageEntities.isEmpty())
            shopRepObj.setImages(imageEntities.stream().map(entity -> (OrganizationImagesRepresentationObject) entity.getRepresentation())
                                                       .collect(toList()));

        return  shopRepObj;
    }
    
    
    
    
    @CacheEvict(allEntries = true, cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
    public ShopResponse shopModification(ShopJsonDTO shopJson) {
        BaseUserEntity baseUser =  securityService.getCurrentUser();
        if(!(baseUser instanceof EmployeeUserEntity)) {
            throw new RuntimeBusinessException(FORBIDDEN, U$AUTH$0001,"shops");
        }
        
        EmployeeUserEntity user = (EmployeeUserEntity)baseUser;
        List<String> userRoles = employeeUserServicehelper.getEmployeeUserRoles(user.getId());
        Long orgId = user.getOrganizationId();
        Long shopId = user.getShopId();
        if (shopJson.getId() == null){
            return createShop(shopJson, orgId, userRoles);
        } else {
            return updateShop(shopJson, orgId, shopId, userRoles);
        }
    }
    
    
    
    

    private ShopResponse createShop(ShopJsonDTO shopJson, Long employeeUserOrgId, List<String> userRoles) {
        if (!userRoles.contains("ORGANIZATION_MANAGER") || !employeeUserOrgId.equals(shopJson.getOrgId())){
        	throw new RuntimeBusinessException(FORBIDDEN, U$AUTH$0001, "this shop");
        }
        
        ShopsEntity shopsEntity = new ShopsEntity();

        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson);

        shopsRepository.save(shopsEntity);
        return new ShopResponse(shopsEntity.getId(), OK);
    }
    
    
    
    

    private ShopResponse updateShop(ShopJsonDTO shopJson, Long employeeUserOrgId, Long employeeUserShopId,
                                    List<String> userRoles) {

        validateShopUpdate(shopJson, employeeUserOrgId, employeeUserShopId, userRoles);
        
    	ShopsEntity shopsEntity = shopsRepository.findById(shopJson.getId()).get();
        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson);
        
        shopsRepository.save(shopsEntity);
        
        return new ShopResponse(shopsEntity.getId(), OK);
    }




	private void validateShopUpdate(ShopJsonDTO shopJson, Long employeeUserOrgId, Long employeeUserShopId,
			List<String> userRoles) {
		
		validateShop(shopJson);
		validateUserToUpdateShop(shopJson, employeeUserOrgId, employeeUserShopId, userRoles);
	}




	private  void validateShop(ShopJsonDTO shopJson)  {
		ShopsEntity shopsEntity = shopsRepository.findById(shopJson.getId()).get();
        if ( shopsEntity == null) {
            throw new RuntimeBusinessException(NOT_FOUND, S$0002, shopJson.getId());
        }
	}




	private void validateUserToUpdateShop(ShopJsonDTO shopJson, Long employeeUserOrgId, Long employeeUserShopId,
			List<String> userRoles) {
        Long orgId = securityService.getCurrentUserOrganizationId();
		if (!userRoles.contains("ORGANIZATION_MANAGER") && !userRoles.contains("STORE_MANAGER")){
        	throw new RuntimeBusinessException(FORBIDDEN, U$AUTH$0001, "this shop");
        }
        if (userRoles.contains("ORGANIZATION_MANAGER") && !employeeUserOrgId.equals(orgId)){
            throw new RuntimeBusinessException(FORBIDDEN, U$AUTH$0001, "this shop");
        }
        if (userRoles.contains("STORE_MANAGER") && !employeeUserShopId.equals(shopJson.getId())){
            throw new RuntimeBusinessException(FORBIDDEN, U$AUTH$0001, "this shop");
        }
	}
    
    
    
    public List<ShopRepresentationObject> getLocationShops(Long orgId, Double longitude, Double latitude, Double radius, String name) {
        Double minLong, maxLong, minLat, maxLat;
        double earthRadius = 6371;
        radius -= 1; // for approximate calculations - not accurate

        minLong = longitude - Math.toDegrees(radius/earthRadius/Math.cos(Math.toRadians(latitude)));
        maxLong = longitude + Math.toDegrees(radius/earthRadius/Math.cos(Math.toRadians(latitude)));

        minLat = latitude - Math.toDegrees(radius/earthRadius);
        maxLat = latitude + Math.toDegrees(radius/earthRadius);

        List<ShopsEntity> shops = new ArrayList<>();
        if (orgId == null)
            shops = shopsRepository.getShopsByLocation(name, minLong, maxLong, minLat, maxLat);
        else
            shops =  shopsRepository.getShopsByLocation(orgId, name, minLong, maxLong, minLat, maxLat);

        return shops.stream().map(s -> (ShopRepresentationObject)s.getRepresentation()).collect(toList());
    }


    public void deleteShop(Long shopId)  {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (!shopsRepository.existsByIdAndOrganizationEntity_Id(shopId, orgId)) {
            throw new RuntimeBusinessException(NOT_FOUND, S$0002, shopId);
        }
        validateShopLinksBeforeDelete(shopId);
        shopsRepository.deleteById(shopId);
    }

    private void validateShopLinksBeforeDelete(Long shopId) {
        List<Long> linkedStocks = stockRepo.findByShopsEntity_Id(shopId)
                                            .stream()
                                            .map(StocksEntity::getId)
                                            .collect(toList());
        if (!linkedStocks.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0001, "stocks "+linkedStocks.toString());
        }


        List<Long> linkedOrders = orderRepo.findByShopsEntityId(shopId)
                                            .stream()
                                            .map(OrdersEntity::getId)
                                            .collect(toList());
        if (!linkedOrders.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0001, "orders "+linkedOrders.toString());
        }

        ShopThreeSixtyEntity linkedShop360 = shopThreeSixtyRepo.findByShopsEntity_Id(shopId);
        if (linkedShop360 != null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0001, "360 shop ("+linkedShop360.getId()+")");
        }


        List<Long> linkedEmployees = empUserRepo.findByShopId(shopId)
                                                .stream()
                                                .map(EmployeeUserEntity::getId)
                                                .collect(toList());
        if (!linkedEmployees.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0001, "employees "+linkedEmployees.toString());
        }
    }
	
}
