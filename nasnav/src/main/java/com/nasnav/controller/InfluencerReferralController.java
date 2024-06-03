package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.InfluencerReferralConstraints;
import com.nasnav.dto.referral_code.InfluencerReferralDto;
import com.nasnav.service.InfluencerReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/influencer-referral")
@RequiredArgsConstructor
public class InfluencerReferralController {

    private final InfluencerReferralService influencerReferralService;

    @Operation(summary = "Registering a new influencer for Referral program", description = "The endpoint to add influencer referral data and his referral code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "406", description = "There is already promotion code with same code!"),
            @ApiResponse(responseCode = "406", description = "User name already exists!"),
            @ApiResponse(responseCode = "406", description = "Passwords Doesn't match!"),

    })
    @PostMapping("/register")
    public ResponseEntity<InfluencerReferralDto> register(@RequestBody InfluencerReferralDto influencerReferralDto) {
        return ResponseEntity.ok(influencerReferralService.register(influencerReferralDto));
    }

    @Operation(summary = "Get total cashback of the influencer by username and password")
    @GetMapping("/cashback")
    public ResponseEntity<InfluencerReferralDto> getWalletBalance(@RequestParam String username,
                                                                  @RequestParam String password) {
        return ResponseEntity.ok(influencerReferralService.getWalletBalance(username, password));
    }

    @Operation(summary = "Get all influencers referrals")
    @GetMapping
    public PaginatedResponse<InfluencerReferralDto> getAllInfluencerReferrals(
                                                    @RequestParam(value = "pageNo", required = false, defaultValue = "0") Integer pageNo,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return influencerReferralService.getAllInfluencerReferrals(pageNo, pageSize);
    }

    @Operation(summary = "Add Or Update the settings for the influencer referral")
    @PutMapping("/{username}/settings")
    public void updateSettings(@PathVariable String username, @RequestBody InfluencerReferralConstraints influencerReferralConstraints) throws JsonProcessingException {
        influencerReferralService.updateReferralSettings(username, influencerReferralConstraints);
    }

}
