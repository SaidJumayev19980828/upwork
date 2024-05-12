package com.nasnav.service.impl;

import com.nasnav.dao.AdvertisementProductRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.AdvertisementProductCompensation;
import com.nasnav.persistence.AdvertisementProductEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.service.AdvertisementProductCustomMapper;
import com.nasnav.service.AdvertisementProductService;
import com.nasnav.service.CompensationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.nasnav.exceptions.ErrorCodes.ADVER$002;

@Service
@AllArgsConstructor
public class AdvertisementProductServiceImpl implements AdvertisementProductService {
    private final AdvertisementProductRepository advertisementProductRepository;
    private final ProductRepository productRepository;
    private final CompensationService compensationService;
    private final AdvertisementProductCustomMapper advertisementProductCustomMapper;

    @Transactional
    public List<AdvertisementProductDTO> save(AdvertisementEntity advertisement, List<AdvertisementProductDTO> advertisementProductDTOS) {
        if (advertisementProductDTOS != null && !advertisementProductDTOS.isEmpty()) {
            return advertisementProductCustomMapper.toDto(advertisementProductRepository.saveAll(mapAdvertisement(advertisement, advertisementProductDTOS)));
        } else {
            return List.of();
        }
    }

    @Transactional
    @Override
    public void deleteAll(Long advertisementId) {
        advertisementProductRepository.deleteAllByAdvertisement_Id(advertisementId);
    }

    private void addCompensation(Set<Long> rules , AdvertisementProductEntity advertisementProduct  ){
        if (rules != null)
            rules.forEach(rule -> advertisementProduct.addCompensationRule(buildCompensation(compensationService.getRule(rule))));
    }



    private AdvertisementProductCompensation buildCompensation(CompensationRulesEntity rule){
        AdvertisementProductCompensation compensation = new AdvertisementProductCompensation();
        compensation.setCompensationRule(rule);
        return compensation;
    }

    private List<AdvertisementProductEntity> mapAdvertisement(AdvertisementEntity advertisement, List<AdvertisementProductDTO> dTOS){
        return dTOS.stream().map(dto-> buildAdvertisementProduct(dto,advertisement)).toList();
    }

    private AdvertisementProductEntity buildAdvertisementProduct(AdvertisementProductDTO dto,AdvertisementEntity advertisement){
        AdvertisementProductEntity advertisementProduct = new AdvertisementProductEntity();
        if (dto.getProductId() != null)
            advertisementProduct.setProduct(productRepository.getById(dto.getProductId()));
        advertisementProduct.setCoins(dto.getCoins());
        advertisementProduct.setLikes(dto.getLikes());
        addCompensation(dto.getCompensationRules(), advertisementProduct);
        advertisementProduct.setAdvertisement(advertisement);
        validCompensationRequest(advertisementProduct);
        return advertisementProduct;
    }

    private void validCompensationRequest(AdvertisementProductEntity advertisementProduct){
        if (advertisementProduct.getCompensationRules() != null && !advertisementProduct.getCompensationRules().isEmpty())
            validateRules(advertisementProduct.getCompensationRules());
    }
    public void validateRules(Set<AdvertisementProductCompensation> advertisementProduct) {
        boolean hasDuplicates = advertisementProduct.stream()
                .map(compensation -> compensation.getCompensationRule().getAction().getId())
                .distinct()
                .count() != advertisementProduct.size();

        if (hasDuplicates) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST , ADVER$002);
        }
    }


}
