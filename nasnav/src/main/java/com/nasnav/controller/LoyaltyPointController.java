package com.nasnav.controller;

import com.nasnav.dto.GiftDTO;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.persistence.*;
import com.nasnav.response.*;
import com.nasnav.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/loyalty")
@Tag(name = "Loyalty Point Controller")
@CrossOrigin("*")
public class LoyaltyPointController {

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;
    @Autowired
    private FamilyService familyService;
    @Autowired
    private TierService tierService;
    @Autowired
    private CharityService charityService;
    @Autowired
    private GiftService giftService;
    @Autowired
    private CoinsDropService coinsDropService;
    @Autowired
    private BoosterService boosterService;


    @PostMapping(value = "config/update")
    public LoyaltyPointsUpdateResponse updateLoyaltyPointConfig(@RequestHeader(name = "User-Token", required = false) String token,
                                                                @RequestBody LoyaltyPointConfigDTO dto) {
        return  loyaltyPointsService.updateLoyaltyPointConfig(dto);
    }

    @DeleteMapping("config/delete")
    public LoyaltyPointDeleteResponse deleteLoyaltyPointConfig(@RequestHeader(name = "User-Token", required = false) String token,
                                                               @RequestParam Long id) {
        return loyaltyPointsService.deleteLoyaltyPointConfig(id);
    }

    @GetMapping(value = "config/list", produces = APPLICATION_JSON_VALUE)
    public List<LoyaltyPointConfigDTO> getLoyaltyPointConfigs(@RequestHeader(name = "User-Token", required = false) String token) {
        return loyaltyPointsService.listLoyaltyPointConfigs();
    }

    @PostMapping(value = "type/update")
    public LoyaltyPointsUpdateResponse updateLoyaltyPointType(@RequestHeader(name = "User-Token", required = false) String token,
                                                              @RequestBody LoyaltyPointTypeDTO dto) {
        return loyaltyPointsService.updateLoyaltyPointType(dto);
    }

    @DeleteMapping("type/delete")
    public LoyaltyPointDeleteResponse deleteLoyaltyPointType(@RequestHeader(name = "User-Token", required = false) String token,
                                                             @RequestParam Long id) {
        return loyaltyPointsService.deleteLoyaltyPointType(id);
    }


    @GetMapping(value = "type/list", produces = APPLICATION_JSON_VALUE)
    public List<LoyaltyPointTypeDTO> getLoyaltyPointTypes(@RequestHeader(name = "User-Token", required = false) String token) {
        return loyaltyPointsService.listLoyaltyPointTypes();
    }


    @PostMapping(value = "points/update")
    public LoyaltyPointsUpdateResponse updateLoyaltyPoint(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestBody LoyaltyPointDTO dto) {
        return loyaltyPointsService.updateLoyaltyPoint(dto);
    }

    @DeleteMapping("points/delete")
    public LoyaltyPointDeleteResponse deleteLoyaltyPoint(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestParam Long id) {
        return loyaltyPointsService.deleteLoyaltyPoint(id);
    }

    @PostMapping("points/terminate")
    public LoyaltyPointsUpdateResponse terminateLoyaltyPoint(@RequestHeader(name = "User-Token", required = false) String token,
                                                             @RequestParam Long id) {
        return loyaltyPointsService.terminateLoyaltyPoint(id);
    }

    @GetMapping(value = "points/list", produces = APPLICATION_JSON_VALUE)
    public List<LoyaltyPointDTO> getLoyaltyPoints(@RequestHeader(name = "User-Token", required = false) String token) {
        return loyaltyPointsService.listOrganizationLoyaltyPoints();
    }

    @GetMapping(value = "points/check", produces = APPLICATION_JSON_VALUE)
    public List<RedeemPointsOfferDTO> checkRedeemPoints(@RequestHeader(name = "User-Token", required = false) String token,
                                                        @RequestParam String code) {
        return loyaltyPointsService.checkRedeemPoints(code);
    }

    @PostMapping(value = "points/redeem")
    public LoyaltyPointsUpdateResponse redeemPoints(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestParam("point_id") Long pointId, @RequestParam("user_id") Long userId) {
        return loyaltyPointsService.redeemPoints(pointId, userId);
    }

    /***
     * Family APIs
     * **/

    @GetMapping(value = "family")
    public Optional<FamilyEntity> getFamilyById(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam(value = "family_id") Long familyId) {
        return familyService.getFamilyById(familyId);
    }

    @PostMapping(value = "family/update")
    public FamilyUpdateResponse createFamily(@RequestHeader(name = "User-Token", required = false) String token,
                                             @RequestBody FamilyDTO dto) {
        return familyService.updateFamily(dto);
    }

    @GetMapping(value = "family/list")
    public List<FamilyEntity> getFamily(@RequestHeader(name = "User-Token", required = false) String token,
                                        @RequestParam(value = "org_id", required = false) Long orgId) {
        if (orgId > 0) {
            return familyService.listFamilyByOrgId(orgId);
        }
        return familyService.listFamily();
    }

    @PostMapping(value = "family/new_member")
    public List<UserEntity> addNewMemberToFamily(@RequestHeader(name = "User-Token", required = false) String token,
                                                 @RequestParam(value = "family_id") Long familyId,
                                                 @RequestParam(value = "user_id") Long userId) {
        if (familyId > 0 && userId > 0) {
            familyService.addNewMemberToFamily(userId, familyId);
        }

        return familyService.getFamilyMembers(familyId);
    }

    @DeleteMapping(value = "family/delete")
    public void deleteFamily(@RequestHeader(name = "User-Token", required = false) String token,
                             @RequestParam(value = "family_id") Long familyId) {
        familyService.deleteFamily(familyId);
    }

    /**
     * Tier APIs & Booster APIs
     **/

    @GetMapping(value = "tier")
    public Optional<TierEntity> getTierById(@RequestHeader(name = "User-Token", required = false) String token,
                                            @RequestParam(value = "tier_id") Long tierId) {
        return tierService.getTierById(tierId);
    }

    @PostMapping(value = "tier/update")
    public TierUpdateResponse createTier(@RequestHeader(name = "User-Token", required = false) String token,
                           @RequestBody TierDTO dto) {
       return tierService.updateTier(dto);
    }

    @GetMapping(value = "tier/list")
    public List<TierEntity> getTier(@RequestHeader(name = "User-Token", required = false) String token,
                                    @RequestParam(value = "org_id", required = false) Long orgId,
                                    @RequestParam(value = "is_special", required = false) Boolean isSpecial) {
        if (orgId > 0) {
            if (isSpecial) {
                return tierService.getTierByOrganization_IdAndIsSpecial(orgId, isSpecial);
            }
            return tierService.getTierByOrganization_Id(orgId);
        }
        return tierService.listTier();
    }

    @DeleteMapping(value = "tier/delete")
    public void deleteTier(@RequestHeader(name = "User-Token", required = false) String token,
                           @RequestParam(value = "tier_id") Long tierId) {
        tierService.deleteTier(tierId);
    }

    @GetMapping(value = "tier/change_user_tier")
    public void addTierToUser(@RequestHeader(name = "User-Token", required = false) String token,
                              @RequestParam(value = "tier_id") Long tierId,
                              @RequestParam(value = "user_id") Long userId) {
        if (tierId > 0 && userId > 0) {
            tierService.addNewTierToUser(userId, tierId);
        }
    }

    @GetMapping(value = "booster")
    public BoosterDTO getBoosterById(@RequestHeader(name = "User-Token", required = false) String token,
                                                  @RequestParam(value = "booster_id") Long boosterId) {
        return boosterService.getBoosterById(boosterId);
    }

    @PostMapping(value = "booster/update")
    public LoyaltyBoosterUpdateResponse createBooster(@RequestHeader(name = "User-Token", required = false) String token,
                                                      @RequestBody BoosterDTO dto) {
        return boosterService.updateBooster(dto);
    }

    @GetMapping(value = "booster/list")
    public List<BoosterDTO> getBooster(@RequestHeader(name = "User-Token", required = false) String token,
                                          @RequestParam(value = "org_id", required = false) Long orgId) {
        if (orgId > 0) {
            return boosterService.getBoosterByOrgId(orgId);
        }
        return boosterService.getBoosters();
    }

    @DeleteMapping(value = "booster/delete")
    public void deleteBooster(@RequestHeader(name = "User-Token", required = false) String token,
                              @RequestParam(value = "booster_id") Long boosterId) {
        boosterService.deleteBooster(boosterId);
    }

    @PostMapping(value = "booster/upgrade")
    public void upgradeUserBooster(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestParam(value = "booster_id") Long boosterId,
                                   @RequestParam(value = "user_id") Long userId) {
        boosterService.upgradeUserBooster(boosterId, userId);
    }

    /**
     * Charity APIs
     *
     */
    @PostMapping(value = "charity/update")
    public CharityUpdateResponse createCharity(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody CharityDTO dto) {
        return charityService.updateCharity(dto);
    }

    @PostMapping(value = "charity/user/update")
    public CharityUpdateResponse createCharity(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody UserCharityDTO dto) {
       return charityService.updateUserCharity(dto);
    }

    @PostMapping(value = "charity/user/donate")
    public void donateUserToCharity(@RequestHeader(name = "User-Token", required = false) String token,
                                    @RequestParam(value = "charity_id") Long charityId,
                                    @RequestParam(value = "user_id") Long userId,
                                    @RequestParam(value = "shop_id") Long shopId) {
        charityService.updateOrCreateLoyaltyUserCharityTransaction(charityId, userId, shopId);
    }

    /**
     * Gift APIs
     *
     */
    @PostMapping(value = "gift/send")
    public GiftUpdateResponse createGift(@RequestHeader(name = "User-Token", required = false) String token,
                                         @RequestBody GiftDTO dto) {
        return giftService.sendGiftFromUserToAnother(dto);
    }

    @GetMapping(value = "gift/all")
    public List<GiftEntity> getUserGifts(@RequestHeader(name = "User-Token", required = false) String token,
                                         @RequestParam(value = "user_id") Long userId) {
        return giftService.getGiftsByUserId(userId);
    }

    @GetMapping(value = "gift/redeem")
    public List<GiftEntity> getUserGiftsNotRedeem(@RequestHeader(name = "User-Token", required = false) String token,
                                                  @RequestParam(value = "user_id") Long userId,
                                                  @RequestParam(value = "is_redeem", required = false) Boolean isRedeem) {
        if (isRedeem) {
            return giftService.getGiftsRedeemByUserReceiveId(userId);
        }
        return giftService.getGiftsNotRedeemByUserId(userId);
    }

    /**
     * Coins Drop APIs
     *
     */
    @PostMapping(value = "coins_drop/update")
    public CoinUpdateResponse createCoin(@RequestHeader(name = "User-Token", required = false) String token,
                                         @RequestBody CoinsDropDTO dto) {
        return coinsDropService.updateCoinsDrop(dto);
    }

    @GetMapping(value = "coins_drop/all")
    public List<CoinsDropEntity> getCoinByOrgId(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestParam(value = "org_id") Long orgId) {
        return coinsDropService.getByOrganizationId(orgId);
    }

}
