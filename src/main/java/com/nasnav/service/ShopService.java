package com.nasnav.service;


import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationImagesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.OrganizationImagesEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import com.nasnav.service.helpers.ShopServiceHelper;

@Service
public class ShopService {

    private final ShopsRepository shopsRepository;
    private final EmployeeUserServiceHelper employeeUserServicehelper;
    private final EmployeeUserRepository employeeUserRepository;
    private final ShopServiceHelper shopServiceHelper;
    private final OrganizationImagesRepository orgImgRepo;
    
    @Autowired
    public ShopService(ShopsRepository shopsRepository, EmployeeUserServiceHelper employeeUserServicehelper,
                       EmployeeUserRepository employeeUserRepository, ShopServiceHelper shopServiceHelper,
                       OrganizationImagesRepository orgImgRepo){
        this.shopsRepository = shopsRepository;
        this.employeeUserServicehelper = employeeUserServicehelper;
        this.employeeUserRepository = employeeUserRepository;
        this.shopServiceHelper = shopServiceHelper;
        this.orgImgRepo = orgImgRepo;
    }
    
    
    

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

    public ShopResponse shopModification(ShopJsonDTO shopJson){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BaseUserEntity user =  employeeUserRepository.getOneByEmail(auth.getName());
        List<String> userRoles = employeeUserServicehelper.getEmployeeUserRoles(user.getId());
        Long employeeUserOrgId = employeeUserRepository.getById(user.getId()).getOrganizationId();
        Long employeeUserShopId = employeeUserRepository.getById(user.getId()).getShopId();
        if (shopJson.getId() == null){
            return createShop(shopJson, employeeUserOrgId, userRoles);
        } else {
            return updateShop(shopJson, employeeUserOrgId, employeeUserShopId, userRoles);
        }
    }

    private ShopResponse createShop(ShopJsonDTO shopJson, Long employeeUserOrgId, List<String> userRoles){
        if (!userRoles.contains("ORGANIZATION_MANAGER") || !employeeUserOrgId.equals(shopJson.getOrgId())){
            return new ShopResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS), HttpStatus.FORBIDDEN);
        }
        
        ShopsEntity shopsEntity = new ShopsEntity();
        BeanUtils.copyProperties(shopJson, shopsEntity);
        //shopsEntity.setOrganizationEntity(organizationRepository.findOneById(shopJson.getOrgId()));
        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson);
        shopsEntity.setCreatedAt(new Date());
        shopsEntity.setUpdatedAt(new Date());
        shopsRepository.save(shopsEntity);
        return new ShopResponse(shopsEntity.getId(), HttpStatus.OK);
    }

    private ShopResponse updateShop(ShopJsonDTO shopJson, Long employeeUserOrgId, Long employeeUserShopId, List<String> userRoles){
        if (!userRoles.contains("ORGANIZATION_MANAGER") && !userRoles.contains("STORE_MANAGER")){
            return new ShopResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS), HttpStatus.FORBIDDEN);
        }
        if (userRoles.contains("ORGANIZATION_MANAGER") && !employeeUserOrgId.equals(shopJson.getOrgId())){
            return new ShopResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS), HttpStatus.FORBIDDEN);
        }
        if (userRoles.contains("STORE_MANAGER") && !employeeUserShopId.equals(shopJson.getId())){
            return new ShopResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS), HttpStatus.FORBIDDEN);
        }
        ShopsEntity shopsEntity = shopsRepository.findById(shopJson.getId()).get();
        if ( shopsEntity == null) {
            return new ShopResponse(Collections.singletonList(ResponseStatus.INVALID_STORE), HttpStatus.NOT_FOUND);
        }
        BeanUtils.copyProperties(shopJson, shopsEntity, shopServiceHelper.getNullProperties(shopJson));
        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson);
        shopsEntity.setUpdatedAt(new Date());
        shopsRepository.save(shopsEntity);
        return new ShopResponse(shopsEntity.getId(), HttpStatus.OK);
    }
    
    
    
    

	
}