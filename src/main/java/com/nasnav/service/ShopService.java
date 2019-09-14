package com.nasnav.service;

import static com.nasnav.persistence.EntityUtils.anyIsNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.ShopResponse;
import com.nasnav.response.StockUpdateResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import com.nasnav.service.helpers.ShopServiceHelper;

@Service
public class ShopService {

    private final ShopsRepository shopsRepository;
    private final EmployeeUserServiceHelper employeeUserServicehelper;
    private final EmployeeUserRepository employeeUserRepository;
    private final ShopServiceHelper shopServiceHelper;
    
    
    @Autowired
    public ShopService(ShopsRepository shopsRepository, EmployeeUserServiceHelper employeeUserServicehelper,
                       EmployeeUserRepository employeeUserRepository, ShopServiceHelper shopServiceHelper){
        this.shopsRepository = shopsRepository;
        this.employeeUserServicehelper = employeeUserServicehelper;
        this.employeeUserRepository = employeeUserRepository;
        this.shopServiceHelper = shopServiceHelper;
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

        return  ((ShopRepresentationObject)shopsEntityOptional.get().getRepresentation());
    }

    public ShopResponse createShop(Long userId, ShopJsonDTO shopJson){
        List<String> userRoles = employeeUserServicehelper.getEmployeeUserRoles(userId);
        Long employeeUserOrgId = employeeUserRepository.getById(userId).getOrganizationId();
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

    public ShopResponse updateShop(Long userId, ShopJsonDTO shopJson){
        List<String> userRoles = employeeUserServicehelper.getEmployeeUserRoles(userId);
        Long employeeUserOrgId = employeeUserRepository.getById(userId).getOrganizationId();
        Long employeeUserShopId = employeeUserRepository.getById(userId).getId();
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
        //shopsEntity.setOrganizationEntity(organizationRepository.findOneById(shopJson.getOrgId()));
        shopsEntity = shopServiceHelper.setAdditionalShopProperties(shopsEntity, shopJson);
        shopsEntity.setUpdatedAt(new Date());
        shopsRepository.save(shopsEntity);
        return new ShopResponse(shopsEntity.getId(), HttpStatus.OK);
    }
    
    
    
    

	
}