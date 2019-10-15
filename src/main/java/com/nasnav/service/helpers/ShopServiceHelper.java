package com.nasnav.service.helpers;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.MallRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.service.SecurityService;

@Service
public class ShopServiceHelper extends BeanUtils{

    private final MallRepository mallRepo;
    
    @Autowired
    private OrganizationRepository orgRepo;
    
    @Autowired
    private SecurityService sercurityService;

    public ShopServiceHelper(MallRepository mallRepository){
        this.mallRepo = mallRepository;
    }

    public String[] getNullProperties(ShopJsonDTO shopJson) {
        final BeanWrapper src = new BeanWrapperImpl(shopJson);
        List<String> nullProperties = new ArrayList<>();
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) nullProperties.add(pd.getName());
        }
        String[] result = new String[nullProperties.size()];
        return nullProperties.toArray(result);
    }

    public ShopsEntity setAdditionalShopProperties(ShopsEntity shopsEntity, ShopJsonDTO shopJson){
        if (shopJson.getMallId() != null){
            shopsEntity.setMallsEntity(mallRepo.findById((shopJson.getMallId())).get());
        }
        if (shopJson.getName() != null) {
            shopsEntity.setPname(StringUtils.encodeUrl(shopJson.getName()));
        }
        if (shopJson.getStreet() != null) {
            shopsEntity.setPStreet(StringUtils.encodeUrl(shopJson.getStreet()));
        }
        
        
        Optional<OrganizationEntity> org = orgRepo.findById(sercurityService.getCurrentUserOrganization());                
        shopsEntity.setOrganizationEntity(org.get());
        
        return shopsEntity;
    }
}