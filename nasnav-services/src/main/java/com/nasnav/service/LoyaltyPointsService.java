package com.nasnav.service;

import com.nasnav.dto.AppliedPointsResponse;
import com.nasnav.dto.SpentPointsInfo;
import com.nasnav.dto.request.LoyaltyPointConfigDTO;
import com.nasnav.dto.request.LoyaltyPointDTO;
import com.nasnav.dto.request.LoyaltyPointTypeDTO;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.dto.response.LoyaltyPointsCartResponseDto;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.OrganizationPoints;
import com.nasnav.response.LoyaltyPointDeleteResponse;
import com.nasnav.response.LoyaltyPointsUpdateResponse;
import com.nasnav.response.LoyaltyUserPointsResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface LoyaltyPointsService {

    LoyaltyPointsUpdateResponse updateLoyaltyPointConfig(LoyaltyPointConfigDTO dto);
    LoyaltyPointDeleteResponse deleteLoyaltyPointConfig(Long id);
    LoyaltyPointsUpdateResponse createLoyaltyPointTransaction(ShopsEntity shop, OrganizationEntity org, UserEntity user, MetaOrderEntity yeshteryMetaOrder,
                                                              OrdersEntity order, BigDecimal points, BigDecimal amount, Integer expiry);
    void createLoyaltyPointTransaction(OrdersEntity order, LoyaltyPointType type, BigDecimal pointsAmount);
    void createYeshteryLoyaltyPointTransaction(MetaOrderEntity yeshteryMetaOrder, LoyaltyPointType type, BigDecimal pointsAmount);
    void redeemPoints(Long orderId, String code);
    List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints(Long orgId );
    List<LoyaltyPointTypeDTO> listLoyaltyPointTypes();
    List<LoyaltyPointConfigDTO> listLoyaltyPointConfigs();
    LoyaltyPointConfigDTO getLoyaltyPointActiveConfig();

    void createLoyaltyPointCharityTransaction(LoyaltyCharityEntity charity, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isDonate);
    LoyaltyPointsUpdateResponse createLoyaltyPointGiftTransaction(LoyaltyGiftEntity gift, UserEntity user, BigDecimal points, Boolean isGift);
    LoyaltyPointsUpdateResponse createLoyaltyPointCoinsDropTransaction(LoyaltyCoinsDropEntity coins, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isCoinsDrop);

    LoyaltyUserPointsResponse getUserPoints(Long orgId);

    List<OrganizationPoints> getUserPointsPerOrg();
    LoyaltyTierDTO getUserOrgTier(Long orgId);

    String generateUserShopPinCode(Long shopId);

    SpentPointsInfo applyPointsOnOrders(Set<Long> points, Set<OrdersEntity> subOrders, BigDecimal totalWithoutShipping,
                                        Long userId, OrganizationEntity org);

    AppliedPointsResponse calculateCartPointsDiscount(List<CartItem> items, Set<Long> points, boolean yeshteryCart);
    List<LoyaltyPointTransactionDTO> getUserSpendablePoints();

    List<LoyaltyPointTransactionDTO> getUserSpendablePoints(Long orgId);

    void givePointsToReferrer(UserEntity user, Long orgId);
    void activateReferralPoints(OrdersEntity suborder);
}
