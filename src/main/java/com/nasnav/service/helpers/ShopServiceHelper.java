package com.nasnav.service.helpers;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.AreaRepository;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dto.AddressDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
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

@Service
public class ShopServiceHelper extends BeanUtils{

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private SecurityService sercurityService;

    @Autowired
    private BrandsRepository brandsRepo;

    @Autowired
    private AreaRepository areaRepo;

    @Autowired
    private AddressRepository addressRepo;
    

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
    
    
    
    

    public ShopsEntity setAdditionalShopProperties(ShopsEntity shopsEntity, ShopJsonDTO shopJson) throws BusinessException {
        if (shopJson.isUpdated("name")) {
            shopsEntity.setName(shopJson.getName());
            shopsEntity.setPname(StringUtils.encodeUrl(shopJson.getName()));
        }

        if (shopJson.isUpdated("address")) {
            AddressesEntity address = null;
            if (shopJson.getAddress() != null) {
                address = new AddressesEntity();
                AddressDTO addressJson = shopJson.getAddress();
                BeanUtils.copyProperties(addressJson, address, new String[]{"id"});

                if (addressJson.getAreaId() != null) {
                    Optional<AreasEntity> area = areaRepo.findById(addressJson.getAreaId());
                    if (!area.isPresent())
                        throw new BusinessException(String.format("Provided area_id (%d) doesn't match any existing area!", addressJson.getAreaId()),
                                "INVALID_PARAM: area_id", HttpStatus.NOT_ACCEPTABLE);
                    address.setAreasEntity(area.get());
                }
                shopsEntity.setAddressesEntity(addressRepo.save(address));
            } else {
                shopsEntity.setAddressesEntity(address);
            }

        }

        if (shopJson.isUpdated("brandId")) {
            if (brandsRepo.findById(shopJson.getBrandId()).isPresent())
                shopsEntity.setBrandId(shopJson.getBrandId());
            else
                throw new BusinessException("Provided brand_id doesn't match any existing brand",
                        "INVALID_PARAM: brand_id", HttpStatus.NOT_ACCEPTABLE);
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