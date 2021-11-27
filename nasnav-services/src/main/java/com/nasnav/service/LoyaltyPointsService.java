package com.nasnav.service;

import com.nasnav.dto.request.LoyaltyPointConfigDTO;
import com.nasnav.dto.request.LoyaltyPointDTO;
import com.nasnav.dto.request.LoyaltyPointTypeDTO;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.persistence.*;
import com.nasnav.response.LoyaltyPointDeleteResponse;
import com.nasnav.response.LoyaltyPointsUpdateResponse;
import com.nasnav.response.LoyaltyUserPointsResponse;

import java.math.BigDecimal;
import java.util.List;

public interface LoyaltyPointsService {

    LoyaltyPointsUpdateResponse updateLoyaltyPointType(LoyaltyPointTypeDTO dto);
    LoyaltyPointsUpdateResponse updateLoyaltyPointConfig(LoyaltyPointConfigDTO dto);
    LoyaltyPointsUpdateResponse updateLoyaltyPoint(LoyaltyPointDTO dto);
    LoyaltyPointDeleteResponse deleteLoyaltyPointType(Long id);
    LoyaltyPointDeleteResponse deleteLoyaltyPoint(Long id);
    LoyaltyPointDeleteResponse deleteLoyaltyPointConfig(Long id);
    LoyaltyPointsUpdateResponse terminateLoyaltyPoint(Long id);
    LoyaltyPointsUpdateResponse updateLoyaltyPointTransaction(ShopsEntity shop, UserEntity user, OrdersEntity order, BigDecimal points, BigDecimal amount);
    void createLoyaltyPointTransaction(OrdersEntity order);
    void createLoyaltyPointTransactionForReturnRequest(ReturnRequestEntity returnRequest);
    LoyaltyPointsUpdateResponse redeemPoints(Long pointId, Long userId);
    List<LoyaltyPointDTO> listOrganizationLoyaltyPoints();
    List<LoyaltyPointTypeDTO> listLoyaltyPointTypes();
    List<LoyaltyPointConfigDTO> listLoyaltyPointConfigs();
    List<RedeemPointsOfferDTO> checkRedeemPoints(String code);

    void updateLoyaltyPointCharityTransaction(LoyaltyCharityEntity charity, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isDonate);
    LoyaltyPointsUpdateResponse updateLoyaltyPointGiftTransaction(LoyaltyGiftEntity gift, UserEntity user, BigDecimal points, Boolean isGift);
    LoyaltyPointsUpdateResponse updateLoyaltyPointCoinsDropTransaction(LoyaltyCoinsDropEntity coins, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isCoinsDrop);

    LoyaltyUserPointsResponse getUserPoints(Long orgId);
    LoyaltyTierDTO getUserOrgTier(Long orgId);
}
