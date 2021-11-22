package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.GiftDTO;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.persistence.*;
import com.nasnav.response.*;
import com.nasnav.service.*;
import com.nasnav.yeshtery.YeshteryConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(LoyaltyPointController.API_PATH)
@Tag(name = "Loyalty Point Controller")
@CrossOrigin("*")
public class LoyaltyPointController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/loyalty";

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;
    @Autowired
    private LoyaltyFamilyService loyaltyFamilyService;
    @Autowired
    private LoyaltyTierService loyaltyTierService;
    @Autowired
    private LoyaltyCharityService loyaltyCharityService;
    @Autowired
    private LoyaltyGiftService loyaltyGiftService;
    @Autowired
    private LoyaltyCoinsDropService loyaltyCoinsDropService;
    @Autowired
    private LoyaltyBoosterService loyaltyBoosterService;
    @Autowired
    private LoyaltyEventService loyaltyEventService;


    @GetMapping(value ="points")
    public LoyaltyUserPointsResponse getUserPoints(@RequestHeader(name = "User-Token", required = false) String token, @RequestParam Long orgId){
        return loyaltyPointsService.getUserPoints(orgId);
    }

    @GetMapping(value ="user_tier")
    public LoyaltyTierDTO getUserTier(@RequestHeader(name = "User-Token", required = false) String token, @RequestParam Long orgId){
        return loyaltyPointsService.getUserOrgTier(orgId);
    }


    // CRUD Ops

    /**
     * Config APIs
     */

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


    /**
     * Type APIs
     */


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


    /***
     * Family APIs
     * **/

    @GetMapping(value = "family")
    public Optional<LoyaltyFamilyEntity> getFamilyById(@RequestHeader(name = "User-Token", required = false) String token,
                                                       @RequestParam(value = "family_id") Long familyId) {
        return loyaltyFamilyService.getFamilyById(familyId);
    }

    @PostMapping(value = "family/update")
    public LoyaltyFamilyUpdateResponse createFamily(@RequestHeader(name = "User-Token", required = false) String token,
                                                    @RequestBody LoyaltyFamilyDTO dto) {
        return loyaltyFamilyService.updateFamily(dto);
    }

    @GetMapping(value = "family/list")
    public List<LoyaltyFamilyEntity> getFamily(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestParam(value = "org_id", required = false, defaultValue = "-1") Long orgId) {
        return loyaltyFamilyService.listFamily(orgId);
    }

    @PostMapping(value = "family/new_member")
    public List<UserEntity> addNewMemberToFamily(@RequestHeader(name = "User-Token", required = false) String token,
                                                 @RequestParam(value = "family_id") Long familyId,
                                                 @RequestParam(value = "user_id") Long userId) {
        if (familyId > 0 && userId > 0) {
            loyaltyFamilyService.addNewMemberToFamily(userId, familyId);
        }

        return loyaltyFamilyService.getFamilyMembers(familyId);
    }

    @DeleteMapping(value = "family/delete")
    public void deleteFamily(@RequestHeader(name = "User-Token", required = false) String token,
                             @RequestParam(value = "family_id") Long familyId) {
        loyaltyFamilyService.deleteFamily(familyId);
    }

    /**
     * Tier APIs & Booster APIs
     **/

    @GetMapping(value = "tier")
    public Optional<LoyaltyTierEntity> getTierById(@RequestHeader(name = "User-Token", required = false) String token,
                                                   @RequestParam(value = "tier_id") Long tierId) {
        return loyaltyTierService.getTierById(tierId);
    }

    @PostMapping(value = "tier/update")
    public LoyaltyTierUpdateResponse createTier(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestBody LoyaltyTierDTO dto) {
        return loyaltyTierService.updateTier(dto);
    }

    @GetMapping(value = "tier/list")
    public List<LoyaltyTierDTO> getTier(@RequestHeader(name = "User-Token", required = false) String token,
                                        @RequestParam(value = "org_id", required = false, defaultValue = "-1") Long orgId,
                                        @RequestParam(value = "is_special", required = false, defaultValue = "false") Boolean isSpecial) {
        return loyaltyTierService.getTiers(orgId, isSpecial);
    }

    @DeleteMapping(value = "tier/delete")
    public void deleteTier(@RequestHeader(name = "User-Token", required = false) String token,
                           @RequestParam(value = "tier_id") Long tierId) {
        loyaltyTierService.deleteTier(tierId);
    }

    @GetMapping(value = "tier/change_user_tier")
    public void addTierToUser(@RequestHeader(name = "User-Token", required = false) String token,
                              @RequestParam(value = "tier_id") Long tierId,
                              @RequestParam(value = "user_id") Long userId) {
        if (tierId > 0 && userId > 0) {
            loyaltyTierService.addNewTierToUser(userId, tierId);
        }
    }

    /**
     * Coins Drop APIs
     *
     */
    @PostMapping(value = "coins_drop/update")
    public LoyaltyCoinUpdateResponse createCoin(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestBody LoyaltyCoinsDropDTO dto) {
        return loyaltyCoinsDropService.updateCoinsDrop(dto);
    }

    @GetMapping(value = "coins_drop/all")
    public List<LoyaltyCoinsDropEntity> getCoinByOrgId(@RequestHeader(name = "User-Token", required = false) String token,
                                                       @RequestParam(value = "org_id") Long orgId) {
        return loyaltyCoinsDropService.getByOrganizationId(orgId);
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

    @GetMapping(value = "booster")
    public LoyaltyBoosterDTO getBoosterById(@RequestHeader(name = "User-Token", required = false) String token,
                                            @RequestParam(value = "booster_id") Long boosterId) {
        return loyaltyBoosterService.getBoosterById(boosterId);
    }

    @PostMapping(value = "booster/update")
    public LoyaltyBoosterUpdateResponse createBooster(@RequestHeader(name = "User-Token", required = false) String token,
                                                      @RequestBody LoyaltyBoosterDTO dto) {
        return loyaltyBoosterService.updateBooster(dto);
    }

    @GetMapping(value = "booster/list")
    public List<LoyaltyBoosterDTO> getBooster(@RequestHeader(name = "User-Token", required = false) String token,
                                              @RequestParam(value = "org_id", required = false, defaultValue = "-1") Long orgId) {
        if (orgId > 0) {
            return loyaltyBoosterService.getBoosterByOrgId(orgId);
        }
        return loyaltyBoosterService.getBoosters();
    }

    @DeleteMapping(value = "booster/delete")
    public void deleteBooster(@RequestHeader(name = "User-Token", required = false) String token,
                              @RequestParam(value = "booster_id") Long boosterId) {
        loyaltyBoosterService.deleteBooster(boosterId);
    }

    @PostMapping(value = "booster/upgrade")
    public void upgradeUserBooster(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestParam(value = "booster_id") Long boosterId,
                                   @RequestParam(value = "user_id") Long userId) {
        loyaltyBoosterService.upgradeUserBooster(boosterId, userId);
    }

    /**
     * Charity APIs
     *
     */
    @PostMapping(value = "charity/update")
    public LoyaltyCharityUpdateResponse createCharity(@RequestHeader(name = "User-Token", required = false) String token,
                                                      @RequestBody LoyaltyCharityDTO dto) {
        return loyaltyCharityService.updateCharity(dto);
    }

    @PostMapping(value = "charity/user/update")
    public LoyaltyCharityUpdateResponse createCharity(@RequestHeader(name = "User-Token", required = false) String token,
                                                      @RequestBody UserCharityDTO dto) {
        return loyaltyCharityService.updateUserCharity(dto);
    }

    @PostMapping(value = "charity/user/donate")
    public void donateUserToCharity(@RequestHeader(name = "User-Token", required = false) String token,
                                    @RequestParam(value = "charity_id") Long charityId,
                                    @RequestParam(value = "user_id") Long userId,
                                    @RequestParam(value = "shop_id") Long shopId) {
        loyaltyCharityService.updateOrCreateLoyaltyUserCharityTransaction(charityId, userId, shopId);
    }

    /**
     * Gift APIs
     *
     */
    @PostMapping(value = "gift/send")
    public LoyaltyGiftUpdateResponse createGift(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestBody GiftDTO dto) {
        return loyaltyGiftService.sendGiftFromUserToAnother(dto);
    }

    @GetMapping(value = "gift/all")
    public List<LoyaltyGiftEntity> getUserGifts(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam(value = "user_id") Long userId) {
        return loyaltyGiftService.getGiftsByUserId(userId);
    }

    @GetMapping(value = "gift/redeem")
    public List<LoyaltyGiftEntity> getUserGiftsNotRedeem(@RequestHeader(name = "User-Token", required = false) String token,
                                                         @RequestParam(value = "user_id") Long userId,
                                                         @RequestParam(value = "is_redeem", required = false) Boolean isRedeem) {
        if (isRedeem) {
            return loyaltyGiftService.getGiftsRedeemByUserReceiveId(userId);
        }
        return loyaltyGiftService.getGiftsNotRedeemByUserId(userId);
    }

    /**
     * Events API
     */

    @GetMapping("event")
    public List<LoyaltyEventDTO> getAllEvents(@RequestHeader(name = "User-Token", required = false) String token, @RequestParam("org_id") Long orgId){
        return loyaltyEventService.getAllEvents(orgId);
    }
    @PostMapping("event")
    public  LoyaltyEventUpdateResponse createUpdateEvent(@RequestHeader(name = "User-Token", required = false) String token, @RequestBody LoyaltyEventDTO request){
        return loyaltyEventService.createUpdateEvent(request);
    }

    @DeleteMapping("event/{id}")
    public void deleteEvent(@RequestHeader(name = "User-Token", required = false) String token, @PathVariable Long id){
        loyaltyEventService.deleteById(id);
    }

}
