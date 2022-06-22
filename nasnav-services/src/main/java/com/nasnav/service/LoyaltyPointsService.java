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
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.OrganizationPoints;
import com.nasnav.response.LoyaltyPointDeleteResponse;
import com.nasnav.response.LoyaltyPointsUpdateResponse;
import com.nasnav.response.LoyaltyUserPointsResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface LoyaltyPointsService {

    LoyaltyPointsUpdateResponse updateLoyaltyPointType(LoyaltyPointTypeDTO dto);
    LoyaltyPointsUpdateResponse updateLoyaltyPointConfig(LoyaltyPointConfigDTO dto);
    LoyaltyPointsUpdateResponse updateLoyaltyPoint(LoyaltyPointDTO dto);
    LoyaltyPointDeleteResponse deleteLoyaltyPointType(Long id);
    LoyaltyPointDeleteResponse deleteLoyaltyPoint(Long id);
    LoyaltyPointDeleteResponse deleteLoyaltyPointConfig(Long id);
    LoyaltyPointsUpdateResponse terminateLoyaltyPoint(Long id);
    LoyaltyPointsUpdateResponse createLoyaltyPointTransaction(ShopsEntity shop, OrganizationEntity org, UserEntity user, MetaOrderEntity yeshteryMetaOrder,
                                                              OrdersEntity order, BigDecimal points, BigDecimal amount, Integer expiry);
    void createLoyaltyPointTransaction(OrdersEntity order, BigDecimal pointsAmount);
    void createYeshteryLoyaltyPointTransaction(MetaOrderEntity yeshteryMetaOrder, BigDecimal pointsAmount);
    void createLoyaltyPointTransactionForReturnRequest(ReturnRequestEntity returnRequest);
    LoyaltyPointsUpdateResponse redeemPoints(Long pointId, Long userId);
    List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints(Long orgId );
    List<LoyaltyPointTypeDTO> listLoyaltyPointTypes();
    List<LoyaltyPointConfigDTO> listLoyaltyPointConfigs();
    LoyaltyPointConfigDTO getLoyaltyPointActiveConfig();
    List<RedeemPointsOfferDTO> checkRedeemPoints(String code);

    void createLoyaltyPointCharityTransaction(LoyaltyCharityEntity charity, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isDonate);
    LoyaltyPointsUpdateResponse createLoyaltyPointGiftTransaction(LoyaltyGiftEntity gift, UserEntity user, BigDecimal points, Boolean isGift);
    LoyaltyPointsUpdateResponse createLoyaltyPointCoinsDropTransaction(LoyaltyCoinsDropEntity coins, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isCoinsDrop);

    LoyaltyUserPointsResponse getUserPoints(Long orgId);

    List<OrganizationPoints> getUserPointsPerOrg();
    LoyaltyTierDTO getUserOrgTier(Long orgId);

    String generateUserShopPinCode(Long shopId);

    List<LoyaltyPointsCartResponseDto> getUserPointsGroupedByOrg(Long yeshteryUserId, List<CartItem> items);

    SpentPointsInfo applyPointsOnOrders(Set<Long> points, Set<OrdersEntity> subOrders, BigDecimal totalWithoutShipping,
                                        Long userId, OrganizationEntity org);

    AppliedPointsResponse calculateCartPointsDiscount(List<CartItem> items, Set<Long> points, boolean yeshteryCart);
    List<LoyaltyPointTransactionDTO> getUserSpendablePoints();
}
