package com.nasnav.service.helpers;


import java.util.ArrayList;
import java.util.List;

import com.nasnav.dao.BrandsRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.service.SecurityService;

import static com.nasnav.exceptions.ErrorCodes.P$BRA$0001;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class ShopServiceHelper extends BeanUtils{

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private SecurityService sercurityService;

    @Autowired
    private BrandsRepository brandsRepo;
    

    public String[] getNullProperties(ShopJsonDTO shopJson) {
        final BeanWrapper src = new BeanWrapperImpl(shopJson);
        List<String> nullProperties = new ArrayList<>();
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) nullProperties.add(pd.getName());
        }
        String[] result = new String[nullProperties.size()];
        nullProperties.remove("banner");
        nullProperties.remove("logo");
        return nullProperties.toArray(result);
    }
    
    
    
    

    public ShopsEntity setAdditionalShopProperties(ShopsEntity shopsEntity, ShopJsonDTO shopJson) {
        if (shopJson.isUpdated("name")) {
            shopsEntity.setName(shopJson.getName());
            shopsEntity.setPname(StringUtils.encodeUrl(shopJson.getName()));
        }

        if (shopJson.isUpdated("country"))
            shopsEntity.setCountry(shopJson.getCountry());

        if (shopJson.isUpdated("street"))
            shopsEntity.setStreet(shopJson.getStreet());

        if (shopJson.isUpdated("streetNumber"))
            shopsEntity.setStreetNumber(shopJson.getStreetNumber());

        if (shopJson.isUpdated("floor"))
            shopsEntity.setFloor(shopJson.getFloor());

        if (shopJson.isUpdated("lat"))
            shopsEntity.setLat(shopJson.getLat());

        if (shopJson.isUpdated("lng"))
            shopsEntity.setLng(shopJson.getLng());

        if (shopJson.isUpdated("brandId")) {
            if (brandsRepo.findById(shopJson.getBrandId()).isPresent()) {
                shopsEntity.setBrandId(shopJson.getBrandId());
            } else {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0001, shopJson.getBrandId());
            }
        }

        if (shopJson.isUpdated("banner"))
            shopsEntity.setBanner(shopJson.getBanner());

        if (shopJson.isUpdated("logo"))
            shopsEntity.setLogo(shopJson.getLogo());

        OrganizationEntity org = sercurityService.getCurrentUserOrganization();               
        shopsEntity.setOrganizationEntity(org);        
        
        return shopsEntity;
    }
}