package com.nasnav.service.helpers;


import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.AreaRepository;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.AREA$001;
import static com.nasnav.exceptions.ErrorCodes.P$BRA$0001;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class ShopServiceHelper extends BeanUtils{

    @Autowired
    private BrandsRepository brandsRepo;

    @Autowired
    private AreaRepository areaRepo;

    @Autowired
    private AddressRepository addressRepo;
    

    public ShopsEntity setAdditionalShopProperties(ShopsEntity shopsEntity, ShopJsonDTO shopJson, OrganizationEntity org) {
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
                    if (!area.isPresent()) {
                        throw new RuntimeBusinessException(NOT_ACCEPTABLE, AREA$001, addressJson.getAreaId());
                    }
                    address.setAreasEntity(area.get());
                }
                shopsEntity.setAddressesEntity(addressRepo.save(address));
            } else {
                shopsEntity.setAddressesEntity(address);
            }

        }

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

        if (shopJson.isUpdated("dark_logo"))
            shopsEntity.setDarkLogo(shopJson.getDarkLogo());

        if (shopJson.isUpdated("placeId"))
            shopsEntity.setPlaceId(shopJson.getPlaceId());

        if (shopJson.isUpdated("priority")) {
            Integer priority = ofNullable(shopJson.getPriority()).orElseGet(() -> 0);
            shopsEntity.setPriority(priority);
        }
        if (shopsEntity.getPriority() == null) {
            shopsEntity.setPriority(0);
        }
        shopsEntity.setOrganizationEntity(org);
        return shopsEntity;
    }
}