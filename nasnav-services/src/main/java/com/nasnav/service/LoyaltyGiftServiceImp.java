package com.nasnav.service;

import com.nasnav.dao.LoyaltyGiftRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.GiftDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.LoyaltyGiftEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.LoyaltyGiftUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.*;
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
    public List<LoyaltyGiftEntity> getGiftsByUserIdAndIsRedeem(Long userId, boolean isRedeem) {
        if (isRedeem) {
            return loyaltyGiftRepository.getByUserTo_IdAndIsRedeemTrue(userId);
        }
        return loyaltyGiftRepository.getByUserFrom_IdAndIsRedeemFalse(userId);
    }

    @Override
    public void updateOrCreateLoyaltyGiftTransaction(Long giftId) {
        LoyaltyGiftEntity loyaltyGiftEntity = loyaltyGiftRepository.findById(giftId).get();
        //
        loyaltyPointsService.createLoyaltyPointGiftTransaction(loyaltyGiftEntity, loyaltyGiftEntity.getUserFrom(), loyaltyGiftEntity.getPoints().negate(), true);
        loyaltyPointsService.createLoyaltyPointGiftTransaction(loyaltyGiftEntity, loyaltyGiftEntity.getUserTo(), loyaltyGiftEntity.getPoints(), false);
    }

    private LoyaltyGiftEntity prepareGiftEntity(GiftDTO dto) {
        LoyaltyGiftEntity entity = new LoyaltyGiftEntity();

        BaseUserEntity currentUser = securityService.getCurrentUser();

        UserEntity fromUser = getUserByEmailAndOrg(currentUser.getEmail(), dto.getOrgId());

        entity.setUserFrom(fromUser);

        UserEntity toUser = getUserByEmailAndOrg(dto.getUserToEmail(), dto.getOrgId());

        entity.setUserTo(toUser);

        entity.setIsActive(Boolean.TRUE);

        if (dto.getIsRedeem() != null) {
            dto.setIsRedeem(dto.getIsRedeem());
        }
        return entity;
    }

    private UserEntity getUserByEmailAndOrg(String email,  Long orgId ) {
        if (!userRepository.existsByEmailIgnoreCaseAndOrganizationId(email, orgId)) {
            return null;
        }
        return userRepository.getByEmailAndOrganizationId(email, orgId);
    }

    private UserEntity checkUserExistsByMobile(String mobile,  Long orgId) {
        if (!userRepository.existsByMobileIgnoreCaseAndOrganizationId(mobile, orgId)) {
            return null;
        }
        return userRepository.getByMobileAndOrganizationId(mobile, orgId);
    }

    private void validateGiftDto(GiftDTO dto) {
        if(isNullOrZero(dto.getOrgId())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001);
        }
        if (allIsNull(dto.getUserToEmail(), dto.getPoints())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, GIFT$PARAM$0001);
        }
    }
}
