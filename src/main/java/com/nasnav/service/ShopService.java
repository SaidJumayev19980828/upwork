package com.nasnav.service;


import static com.nasnav.cache.Caches.ORGANIZATIONS_SHOPS;
import static com.nasnav.cache.Caches.SHOPS_BY_ID;
import static com.nasnav.exceptions.ErrorCodes.S$0001;
import static com.nasnav.exceptions.ErrorCodes.S$0002;
import static com.nasnav.exceptions.ErrorCodes.S$0003;
import static com.nasnav.exceptions.ErrorCodes.U$AUTH$0001;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.cache.annotation.CacheResult;

import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationImagesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.service.helpers.ShopServiceHelper;

@Service
public class ShopService {
	
	@Autowired
	private SecurityService securityService;

    private final ShopsRepository shopsRepository;
    private final UserServicesHelper userServicehelper;
    private final ShopServiceHelper shopServiceHelper;
    private final OrganizationImagesRepository orgImgRepo;

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private EmployeeUserRepository empUserRepo;
    
    @Autowired
    public ShopService(ShopsRepository shopsRepository, UserServicesHelper userServicehelper,
                       ShopServiceHelper shopServiceHelper, OrganizationImagesRepository orgImgRepo){
        this.shopsRepository = shopsRepository;
        this.userServicehelper = userServicehelper;
        this.shopServiceHelper = shopServiceHelper;
        this.orgImgRepo = orgImgRepo;
    }
    
    
    

    
    @CacheResult(cacheName = ORGANIZATIONS_SHOPS)
    public List<ShopRepresentationObject> getOrganizationShops(Long organizationId, boolean showWarehouses) {
    	//TODO this filtering is better to be done in the where condition of a single jpa query similar to
    	//similar to ProductRepository.find360Collections
    	//but i was in hurry
    	List<ShopsEntity> shopsEntities = emptyList();
    	if(showWarehouses) {
    		shopsEntities = shopsRepository.findByOrganizationEntity_IdAndRemoved(organizationId, 0);
    	}else {
    		shopsEntities = shopsRepository.findByOrganizationEntity_IdAndRemovedAndIsWarehouse(organizationId, 0, 0);	
    	}

        if(shopsEntities==null || shopsEntities.isEmpty())
            throw new RuntimeBusinessException(NOT_FOUND, S$0003);

        return shopsEntities
        		.stream()
        		.map(shopsEntity -> {
			            ShopRepresentationObject shopRepresentationObject = ((ShopRepresentationObject) shopsEntity.getRepresentation());
			            //TODO why working days won't be returned from the API unlike getShopById API
			            shopRepresentationObject.setOpenWorkingDays(null);
			            return shopRepresentationObject;
        		})
        		.collect(toList());
    }
    
    
    

    
//    @CacheResult(cacheName = "shops_by_id")
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
    
    
    
    public List<ShopRepresentationObject> getLocationShops(Long orgId, String name) {

        Set<ShopsEntity> shops = shopsRepository.getShopsByLocation(name);

        return shops.stream().map(s -> (ShopRepresentationObject)s.getRepresentation()).collect(toList());
    }


    @Transactional
    @CacheEvict(cacheNames = {ORGANIZATIONS_SHOPS, SHOPS_BY_ID})
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
