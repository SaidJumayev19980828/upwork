package com.nasnav.service.helpers;


import com.nasnav.dao.MallRepository;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.ShopsEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShopServiceHelper extends BeanUtils{

    private final MallRepository mallRepository;

    public ShopServiceHelper(MallRepository mallRepository){
        this.mallRepository = mallRepository;
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
            shopsEntity.setMallsEntity(mallRepository.findById((shopJson.getMallId())).get());
        }
        if (shopJson.getName() != null) {
            shopsEntity.setPname(EntityUtils.encodeUrl(shopJson.getName()));
        }
        if (shopJson.getStreet() != null) {
            shopsEntity.setPStreet(EntityUtils.encodeUrl(shopJson.getStreet()));
        }
        return shopsEntity;
    }
}