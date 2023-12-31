package com.nasnav.controller;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.response.*;
import com.nasnav.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/loyalty")
@CrossOrigin("*")
public class LoyaltyPointController {

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;
    @Autowired
    private LoyaltyTierService loyaltyTierService;


    @GetMapping(value ="points")
    public LoyaltyUserPointsResponse getPointsToUser(@RequestHeader(name = "User-Token", required = false) String token){
        return loyaltyPointsService.getUserPoints();
    }

    @PostMapping(value ="share_points")
    public void sharePoints(@RequestHeader(name = "User-Token", required = false) String token , @RequestParam("org_id")  Long orgId , @RequestParam("email")  String email ,@RequestParam("points") BigDecimal points){
         loyaltyPointsService.sharePoints(orgId ,email,points);
    }
    @GetMapping(value ="spendable_points")
    public List<LoyaltyPointTransactionDTO> getUserSpendablePoints(@RequestHeader(name = "User-Token", required = false) String token){
        return loyaltyPointsService.getUserSpendablePointsForAuthUserOrganization();
    }

    @GetMapping(value = "points/list", produces = APPLICATION_JSON_VALUE)
    public List<LoyaltyPointTransactionDTO> getLoyaltyPoints(@RequestHeader(name = "User-Token", required = false) String token) {
        return loyaltyPointsService.listOrganizationLoyaltyPoints();
    }

    @GetMapping(value = "points/list_by_user", produces = APPLICATION_JSON_VALUE)
    public List<LoyaltyPointTransactionDTO> getLoyaltyPoints(@RequestHeader(name = "User-Token", required = false) String token, @RequestParam("user_id") Long userId) {
        return loyaltyPointsService.listOrganizationLoyaltyPointsByUser(userId);
    }

    @GetMapping(value ="user_tier")
    public LoyaltyTierDTO getUserTier(@RequestHeader(name = "User-Token", required = false) String token){
        return loyaltyPointsService.getUserOrgTier();
    }

    // CRUD Ops

    /**
     * Config APIs
     */

    @PostMapping(value = "config/update")
    public LoyaltyPointsUpdateResponse updateLoyaltyPointConfig(@RequestHeader(name = "User-Token", required = false) String token,
                                                                @RequestBody LoyaltyPointConfigDTO dto) {
        return loyaltyPointsService.updateLoyaltyPointConfig(dto);
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

    @GetMapping(value = "config", produces = APPLICATION_JSON_VALUE)
    public LoyaltyPointConfigDTO getLoyaltyPointConfig(@RequestHeader(name = "User-Token", required = false) String token) {
        return loyaltyPointsService.getLoyaltyPointActiveConfig();
    }


    /**
     * Tier APIs APIs
     **/

    @GetMapping(value = "tier")
    public LoyaltyTierDTO getTierById(@RequestHeader(name = "User-Token", required = false) String token,
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
                                        @RequestParam(value = "is_special", required = false) Boolean isSpecial) {
        return loyaltyTierService.getTiers(isSpecial);
    }

    @DeleteMapping(value = "tier/delete")
    public void deleteTier(@RequestHeader(name = "User-Token", required = false) String token,
                           @RequestParam(value = "tier_id") Long tierId) {
        loyaltyTierService.deleteTier(tierId);
    }

    @PostMapping(value = "tier/change_user_tier")
    public UserRepresentationObject changeUserTier(@RequestHeader(name = "User-Token", required = false) String token,
                                                   @RequestParam(value = "tier_id") Long tierId,
                                                   @RequestParam(value = "user_id") Long userId) {
        return loyaltyTierService.changeUserTier(userId, tierId);
    }

    @PostMapping(value = "points/code/redeem")
    public void redeemPoints(@RequestHeader(name = "User-Token", required = false) String token,
                             @RequestParam("order_id") Long orderId,
                             @RequestParam("code") String code) {
        loyaltyPointsService.redeemPoints(orderId, code);
    }


    @GetMapping(value = "points/code/generate")
    public String userScan(@RequestHeader(name = "User-Token", required = false) String token,
                           @RequestParam("shop_id") Long shopId) {
        return loyaltyPointsService.generateUserShopPinCode(shopId);
    }
}
