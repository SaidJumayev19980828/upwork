package com.nasnav.service;

import com.nasnav.dao.GiftRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.GiftDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.GiftEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.GiftUpdateResponse;
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
public class GiftServiceImp implements GiftService {

    @Autowired
    SecurityService securityService;
    @Autowired
    GiftRepository giftRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LoyaltyPointsService loyaltyPointsService;

    @Override
    public GiftUpdateResponse sendGiftFromUserToAnother(GiftDTO dto) {
        validateGiftDto(dto);

        GiftEntity giftEntity = prepareGiftEntity(dto);
        giftRepository.save(giftEntity);
        return new GiftUpdateResponse(giftEntity.getId());
    }

    @Override
    public List<GiftEntity> getGiftsByUserId(Long userId) {
        return giftRepository.getByUserFrom_Id(userId);
    }

    @Override
    public List<GiftEntity> getGiftsNotRedeemByUserId(Long userId) {
        return giftRepository.getByUserFrom_IdAndIsRedeemFalse(userId);
    }

    @Override
    public List<GiftEntity> getGiftsRedeemByUserReceiveId(Long userId) {
        return giftRepository.getByUserTo_IdAndIsRedeemTrue(userId);
    }

    @Override
    public void updateOrCreateLoyaltyGiftTransaction(Long giftId) {
        GiftEntity giftEntity = giftRepository.findById(giftId).get();
        //
        loyaltyPointsService.updateLoyaltyPointGiftTransaction(giftEntity, giftEntity.getUserFrom(), giftEntity.getPoints() * -1, true);
        loyaltyPointsService.updateLoyaltyPointGiftTransaction(giftEntity, giftEntity.getUserTo(), giftEntity.getPoints(), false);
    }

    private GiftEntity prepareGiftEntity(GiftDTO dto) {
        GiftEntity entity = new GiftEntity();
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
