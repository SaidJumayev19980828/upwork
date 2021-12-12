package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.request.LoyaltyCharityDTO;
import com.nasnav.dto.request.UserCharityDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.LoyaltyCharityUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.math.BigDecimal.ZERO;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class LoyaltyCharityServiceImp implements LoyaltyCharityService {

    @Autowired
    LoyaltyCharityRepository loyaltyCharityRepository;
    @Autowired
    UserCharityRepository userCharityRepository;
    @Autowired
    LoyaltyPointTransactionRepository loyaltyTransactionRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    LoyaltyPointsService loyaltyPointsService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ShopsRepository shopsRepository;

    @Override
    public LoyaltyCharityUpdateResponse updateCharity(LoyaltyCharityDTO dto) {
        validateCharityDTO(dto);
        LoyaltyCharityEntity entity = prepareCharityEntity(dto);
        loyaltyCharityRepository.save(entity);
        return new LoyaltyCharityUpdateResponse(entity.getId());
    }

    @Override
    public LoyaltyCharityUpdateResponse updateUserCharity(UserCharityDTO dto) {
        validateUserCharityDTO(dto);
        UserCharityEntity entity = prepareUserCharityEntity(dto);
        userCharityRepository.save(entity);
        return new LoyaltyCharityUpdateResponse(entity.getId());
    }

    @Override
    public void updateOrCreateLoyaltyUserCharityTransaction(Long charityId, Long userId, Long shopId) {
        Optional<LoyaltyCharityEntity> charityEntityOptional = loyaltyCharityRepository.findById(charityId);
        if(!charityEntityOptional.isPresent()){
            throw new RuntimeBusinessException(NOT_FOUND, CHARITY$PARAM$0002, charityId);
        }
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId);
        if(!userEntityOptional.isPresent()){
            throw new RuntimeBusinessException(NOT_FOUND, CHARITY$PARAM$0002, userId);
        }

        Optional<ShopsEntity> shopOptional = shopsRepository.findById(shopId);
        if(!shopOptional.isPresent()){
            throw new RuntimeBusinessException(NOT_FOUND, S$0002, shopId);
        }
        ShopsEntity shopEntity = shopOptional.get();

        LoyaltyCharityEntity loyaltyCharityEntity = charityEntityOptional.get();
        UserEntity userEntity = userEntityOptional.get();

        //
        Integer userTotalPoint = loyaltyTransactionRepository.findAllRedeemablePoints(userId);
        Optional<UserCharityEntity> userDonation = userCharityRepository.findByUser_IdAndCharity_Id( userId, charityId);
        if(!userDonation.isPresent()){
            throw new RuntimeBusinessException(NOT_FOUND, CHARITY$PARAM$0003, userId);
        }
        Integer userDonationPercentage = userDonation.get().getDonationPercentage();

        Integer donationValue = 0;

        if (userTotalPoint != null &&  userTotalPoint > 0 && userDonationPercentage > 0) {
            donationValue = userTotalPoint * (userDonationPercentage / 100);
        }

        BigDecimal charityTotalPoint = loyaltyTransactionRepository.getByCharity_Id(charityId)
                .stream()
                .map(i -> i.getPoints())
                .reduce(ZERO , BigDecimal::add);
        charityTotalPoint = charityTotalPoint.add(BigDecimal.valueOf(donationValue.longValue()));

        loyaltyPointsService.createLoyaltyPointCharityTransaction(loyaltyCharityEntity, userEntity, charityTotalPoint, shopEntity, true);
        loyaltyPointsService.createLoyaltyPointCharityTransaction(loyaltyCharityEntity, userEntity, charityTotalPoint.negate(), shopEntity, false);

    }

    private LoyaltyCharityEntity prepareCharityEntity(LoyaltyCharityDTO dto) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        LoyaltyCharityEntity entity = loyaltyCharityRepository.getByIdAndOrganization_Id(dto.getId(), orgId)
                .orElseGet(LoyaltyCharityEntity::new);
        if (dto.getCharityName() != null) {
            entity.setCharityName(dto.getCharityName());
        }
        if (dto.getTotalDonation() != null) {
            entity.setTotalDonation(dto.getTotalDonation());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        } else {
            entity.setIsActive(true);
        }
        entity.setOrganization(org);
        return entity;
    }

    private UserCharityEntity prepareUserCharityEntity(UserCharityDTO dto) {
        UserCharityEntity entity = userCharityRepository.findByIdAndUser_IdAndCharity_Id(dto.getId(), dto.getUserId(), dto.getCharityId())
                .orElseGet(UserCharityEntity::new);
        if (dto.getUserId() != null) {
            UserEntity user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0002, dto.getUserId()));
            entity.setUser(user);
        }
        if (dto.getCharityId() != null) {
            LoyaltyCharityEntity charity = loyaltyCharityRepository.findById(dto.getCharityId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0002, dto.getCharityId()));
            entity.setCharity(charity);
        }
        if (dto.getDonationPercentage() != null) {
            entity.setDonationPercentage(dto.getDonationPercentage());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        } else {
            entity.setIsActive(true);
        }
        return entity;
    }

    private void validateCharityDTO(LoyaltyCharityDTO dto) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (dto.getId() != null && dto.getId() > 0 ) {
            if (!loyaltyCharityRepository.existsByIdAndOrganization_Id(dto.getId(), orgId)) {
                throw new RuntimeBusinessException(NOT_FOUND, CHARITY$PARAM$0002, dto.getId());
            }
        }
    }

    private void validateUserCharityDTO(UserCharityDTO dto) {
        if (dto.getId() != null && dto.getId() > 0) {
            if (!userCharityRepository.existsByIdAndUser_IdAndCharity_Id(dto.getId(), dto.getUserId(), dto.getCharityId())) {
                throw new RuntimeBusinessException(NOT_FOUND, CHARITY$PARAM$0002, dto.getId());
            }
        }
    }
}
