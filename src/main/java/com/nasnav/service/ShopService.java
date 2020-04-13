package com.nasnav.service;


import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.cache.annotation.CacheResult;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationImagesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationImagesEntity;
import com.nasnav.persistence.ShopsEntity;
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
    public ShopService(ShopsRepository shopsRepository, EmployeeUserServiceHelper employeeUserServicehelper,
                       EmployeeUserRepository employeeUserRepository, ShopServiceHelper shopServiceHelper,
                       OrganizationImagesRepository orgImgRepo){
        this.shopsRepository = shopsRepository;
        this.employeeUserServicehelper = employeeUserServicehelper;
        this.shopServiceHelper = shopServiceHelper;
        this.orgImgRepo = orgImgRepo;
    }
    
    
    

    
    @CacheResult(cacheName = "organizations_shops")
    public List<ShopRepresentationObject> getOrganizationShops(Long organizationId) throws BusinessException {

        List<ShopsEntity> shopsEntities = shopsRepository.findByOrganizationEntity_Id(organizationId);

        if(shopsEntities==null || shopsEntities.isEmpty())
            throw new BusinessException("No shops found",null, HttpStatus.NOT_FOUND);

        return shopsEntities.stream().map(shopsEntity -> {
            ShopRepresentationObject shopRepresentationObject = ((ShopRepresentationObject) shopsEntity.getRepresentation());
            //TODO why working days won't be returned from the API unlike getShopById API
            shopRepresentationObject.setOpenWorkingDays(null);
            return shopRepresentationObject;
        }).collect(Collectors.toList());
    }
    
    
    

    
//    @CacheResult(cacheName = "shops_by_id")
    public ShopRepresentationObject getShopById(Long shopId) throws BusinessException {

        Optional<ShopsEntity> shopsEntityOptional = shopsRepository.findById(shopId);

        if(shopsEntityOptional==null || !shopsEntityOptional.isPresent())
            throw new BusinessException("Shop not found",null, HttpStatus.NOT_FOUND);

        ShopRepresentationObject shopRepObj = (ShopRepresentationObject)shopsEntityOptional.get().getRepresentation();
        List<OrganizationImagesEntity> imageEntities = orgImgRepo.findByShopsEntityId(shopId);
        if(imageEntities != null && !imageEntities.isEmpty())
            shopRepObj.setImages(imageEntities.stream().map(entity -> (OrganizationImagesRepresentationObject) entity.getRepresentation())
                                                       .collect(Collectors.toList()));

        return  shopRepObj;
    }
    
    
    
    
    @CacheEvict(allEntries = true, cacheNames = {"organizations_shops", "shops_by_id"})
    public ShopResponse shopModification(ShopJsonDTO shopJson) throws BusinessException{
        BaseUserEntity baseUser =  securityService.getCurrentUser();
        if(!(baseUser instanceof EmployeeUserEntity)) {
        	throw new BusinessException("User is not an authorized to modify shops!", "INSUFFICIENT RIGHTS", HttpStatus.FORBIDDEN);
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
    
    
    
    

    private ShopResponse createShop(ShopJsonDTO shopJson, Long employeeUserOrgId, List<String> userRoles) throws BusinessException{
        if (!userRoles.contains("ORGANIZATION_MANAGER") || !employeeUserOrgId.equals(shopJson.getOrgId())){
        	throw new BusinessException("User is not an authorized to modify this shop!", "INSUFFICIENT RIGHTS", HttpStatus.FORBIDDEN);
        }
        
        ShopsEntity shopsEntity = new ShopsEntity();
        BeanUtils.copyProperties(shopJson, shopsEntity);
        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson);
        shopsRepository.save(shopsEntity);
        return new ShopResponse(shopsEntity.getId(), HttpStatus.OK);
    }
    
    
    
    

    private ShopResponse updateShop(ShopJsonDTO shopJson, Long employeeUserOrgId, Long employeeUserShopId, List<String> userRoles) throws BusinessException{
    	
    	validateShopUdpate(shopJson, employeeUserOrgId, employeeUserShopId, userRoles);
        
    	ShopsEntity shopsEntity = shopsRepository.findById(shopJson.getId()).get();
        BeanUtils.copyProperties(shopJson, shopsEntity, shopServiceHelper.getNullProperties(shopJson));
        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson);
        
        shopsRepository.save(shopsEntity);
        
        return new ShopResponse(shopsEntity.getId(), HttpStatus.OK);
    }




	private void validateShopUdpate(ShopJsonDTO shopJson, Long employeeUserOrgId, Long employeeUserShopId,
			List<String> userRoles) throws BusinessException {
		
		validateShop(shopJson);
		validateUserToUpdateShop(shopJson, employeeUserOrgId, employeeUserShopId, userRoles);
	}




	private  void validateShop(ShopJsonDTO shopJson) throws BusinessException {
		ShopsEntity shopsEntity = shopsRepository.findById(shopJson.getId()).get();
        if ( shopsEntity == null) {
        	throw new BusinessException(format("No shop exists with id[%d]!",shopJson.getId()), "INVALID STORE", HttpStatus.NOT_FOUND);
        }
	}




	private void validateUserToUpdateShop(ShopJsonDTO shopJson, Long employeeUserOrgId, Long employeeUserShopId,
			List<String> userRoles) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();
		if (!userRoles.contains("ORGANIZATION_MANAGER") && !userRoles.contains("STORE_MANAGER")){
        	throw new BusinessException("User is not an authorized to modify this shop!", "INSUFFICIENT RIGHTS", HttpStatus.FORBIDDEN);
        }
        if (userRoles.contains("ORGANIZATION_MANAGER") && !employeeUserOrgId.equals(orgId)){
        	throw new BusinessException("User is not an authorized to modify this shop!", "INSUFFICIENT RIGHTS", HttpStatus.FORBIDDEN);
        }
        if (userRoles.contains("STORE_MANAGER") && !employeeUserShopId.equals(shopJson.getId())){
        	throw new BusinessException("User is not an authorized to modify this shop!", "INSUFFICIENT RIGHTS", HttpStatus.FORBIDDEN);
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

        return shops.stream().map(s -> (ShopRepresentationObject)s.getRepresentation()).collect(Collectors.toList());
    }

	
}
