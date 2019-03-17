package com.nasnav.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ShopsEntity;

@Service
public class ShopService {


    private final ShopsRepository shopsRepository;

    @Autowired
    public ShopService(ShopsRepository shopsRepository){
        this.shopsRepository = shopsRepository;
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

}