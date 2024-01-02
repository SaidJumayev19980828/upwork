package com.nasnav.service;

import com.nasnav.dto.AppliedPointsResponse;
import com.nasnav.dto.SpentPointsInfo;
import com.nasnav.dto.request.LoyaltyPointConfigDTO;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
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
    void sharePoints(Long orgId, String email , BigDecimal points);
    List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints(Long orgId );
    List<LoyaltyPointConfigDTO> listLoyaltyPointConfigs();
    LoyaltyPointConfigDTO getLoyaltyPointActiveConfig();

    LoyaltyUserPointsResponse getUserPoints();

    LoyaltyUserPointsResponse getUserPoints(Long orgId);

    List<OrganizationPoints> getUserPointsPerOrg();
    LoyaltyTierDTO getUserOrgTier(Long orgId);

    String generateUserShopPinCode(Long shopId);

    SpentPointsInfo applyPointsOnOrders(Set<Long> points, Set<OrdersEntity> subOrders, BigDecimal totalWithoutShipping,
                                        Long userId, OrganizationEntity org);

    AppliedPointsResponse calculateCartPointsDiscount(List<CartItem> items, Set<Long> points, boolean yeshteryCart);
    List<LoyaltyPointTransactionDTO> getUserSpendablePointsForCartOrganizations();

    List<LoyaltyPointTransactionDTO> getUserSpendablePointsForOrganization(Long orgId);

    List<LoyaltyPointTransactionDTO> getUserSpendablePointsForAuthUserOrganization();

    void givePointsToReferrer(UserEntity user, Long orgId);
    void activateReferralPoints(OrdersEntity suborder);
	List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints();
	List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPointsByUser(Long userId);
    LoyaltyTierDTO getUserOrgTier();
	List<LoyaltyPointConfigDTO> listLoyaltyPointConfigsForAllOrganizations();
}
