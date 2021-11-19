package com.nasnav.service;

import com.nasnav.dao.LoyaltyGiftRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.GiftDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.LoyaltyGiftEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.LoyaltyGiftUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class LoyaltyGiftServiceImp implements LoyaltyGiftService {

    @Autowired
    SecurityService securityService;
    @Autowired
    LoyaltyGiftRepository loyaltyGiftRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LoyaltyPointsService loyaltyPointsService;

    @Override
    public LoyaltyGiftUpdateResponse sendGiftFromUserToAnother(GiftDTO dto) {
        validateGiftDto(dto);

        LoyaltyGiftEntity loyaltyGiftEntity = prepareGiftEntity(dto);
        loyaltyGiftRepository.save(loyaltyGiftEntity);
        return new LoyaltyGiftUpdateResponse(loyaltyGiftEntity.getId());
    }

    @Override
    public List<LoyaltyGiftEntity> getGiftsByUserId(Long userId) {
        return loyaltyGiftRepository.getByUserFrom_Id(userId);
    }

    @Override
    public List<LoyaltyGiftEntity> getGiftsNotRedeemByUserId(Long userId) {
        return loyaltyGiftRepository.getByUserFrom_IdAndIsRedeemFalse(userId);
    }

    @Override
    public List<LoyaltyGiftEntity> getGiftsRedeemByUserReceiveId(Long userId) {
        return loyaltyGiftRepository.getByUserTo_IdAndIsRedeemTrue(userId);
    }

    @Override
    public void updateOrCreateLoyaltyGiftTransaction(Long giftId) {
        LoyaltyGiftEntity loyaltyGiftEntity = loyaltyGiftRepository.findById(giftId).get();
        //
        loyaltyPointsService.updateLoyaltyPointGiftTransaction(loyaltyGiftEntity, loyaltyGiftEntity.getUserFrom(), loyaltyGiftEntity.getPoints() * -1, true);
        loyaltyPointsService.updateLoyaltyPointGiftTransaction(loyaltyGiftEntity, loyaltyGiftEntity.getUserTo(), loyaltyGiftEntity.getPoints(), false);
    }

    private LoyaltyGiftEntity prepareGiftEntity(GiftDTO dto) {
        LoyaltyGiftEntity entity = new LoyaltyGiftEntity();
        Optional<UserEntity> userFromOptional = userRepository.findById(dto.getUserFromId());

        if(!userFromOptional.isPresent()){
            throw new RuntimeBusinessException(NOT_FOUND, U$0001, dto.getUserFromId());
        }

        UserEntity userFrom = userFromOptional.get();
        entity.setUserFrom(userFrom);
        if (dto.getPoints() != null) {
            entity.setPoints(dto.getPoints());
        }
        if (dto.getIsActive()) {
            entity.setIsActive(dto.getIsActive());
        }

        Optional<UserEntity> userToOptional = userRepository.findById(dto.getUserToId());

        if(!userToOptional.isPresent()){
            throw new RuntimeBusinessException(NOT_FOUND, U$0001, dto.getUserToId());
        }

        entity.setUserTo(userToOptional.get());

        if(!anyIsNull(dto.getEmail())){
            entity.setEmail(dto.getEmail());
        }
        if(!anyIsNull(dto.getPhoneNumber())){
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getIsRedeem()) {
            dto.setIsRedeem(dto.getIsRedeem());
        }
        return entity;
    }

    private UserEntity checkUserExistsByEmail(String email) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (!userRepository.existsByEmailIgnoreCaseAndOrganizationId(email, orgId)) {
            return null;
        }
        return userRepository.getByEmailAndOrganizationId(email, orgId);
    }

    private UserEntity checkUserExistsByMobile(String mobile) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (!userRepository.existsByMobileIgnoreCaseAndOrganizationId(mobile, orgId)) {
            return null;
        }
        return userRepository.getByMobileAndOrganizationId(mobile, orgId);
    }

    private void validateGiftDto(GiftDTO dto) {
        if (anyIsNull(dto, dto.getUserFromId(), dto.getPoints())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, GIFT$PARAM$0001);
        } else if (allIsNull(dto.getEmail(), dto.getPhoneNumber())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, GIFT$PARAM$0001);
        }
    }
}
