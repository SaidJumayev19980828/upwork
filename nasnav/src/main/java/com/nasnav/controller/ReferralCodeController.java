package com.nasnav.controller;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.dto.referral_code.ReferralStatsDto;
import com.nasnav.dto.referral_code.ReferralTransactionsDto;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.service.ReferralCodeService;
import com.nasnav.service.ReferralSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(ReferralCodeController.API_PATH)
@CrossOrigin("*")
public class ReferralCodeController {

    static final String API_PATH = "/referral";

    @Autowired
    private ReferralCodeService referralCodeService;

    @Autowired
    private ReferralSettingsService referralSettingsService;

    @PostMapping(value = "/organization/settings", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public ReferralSettingsDto createSettings(@RequestBody ReferralSettingsDto referralSettingsDto){
        return referralSettingsService.create(referralSettingsDto);
    }

    @GetMapping("/list")
    public PaginatedResponse<ReferralCodeDto> getList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return referralCodeService.getList(pageNo, pageSize);
    }

    @GetMapping("/childs")
    public PaginatedResponse<ReferralTransactionsDto> getChilds(
                                                      @RequestParam(value = "type") ReferralTransactionsType type,
                                                    @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return referralCodeService.getChilds(type, pageNo, pageSize);
    }


    @GetMapping("/user")
    public ReferralCodeDto get(){
        return referralCodeService.getForUser();
    }

    @GetMapping("/code/{code}")
    public ReferralCodeDto get(@PathVariable("code") String code){
        return referralCodeService.get(code);
    }


    @PostMapping("/sendOtp")
    public void send(@RequestParam("phoneNumber") String phoneNumber,
                     @RequestParam(value = "parentReferralCode", required = false) String parentReferralCode) {
         referralCodeService.send(phoneNumber, parentReferralCode);
    }

    @PostMapping("/validateOtp/{referral_token}")
    public ReferralCodeDto validate(@PathVariable("referral_token") String token) {
        return referralCodeService.validateReferralOtp(token);
    }

    @PostMapping("/activate/{referralCode}")
    public void activate(@PathVariable("referralCode") String referralCode){
        referralCodeService.activate(referralCode);
    }

    @PostMapping("/deactivate/{referralCode}")
    public void deActivate(@PathVariable("referralCode") String referralCode ){
        referralCodeService.deActivate(referralCode);
    }


    @GetMapping("/stats")
    public ReferralStatsDto getStats(){
        return referralCodeService.getStats();
    }

}
