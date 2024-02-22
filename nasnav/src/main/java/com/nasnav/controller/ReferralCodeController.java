package com.nasnav.controller;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeCreateResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralStatsDto;
import com.nasnav.service.ReferralCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;

@RestController
@RequestMapping(ReferralCodeController.API_PATH)
@CrossOrigin("*")
public class ReferralCodeController {

    static final String API_PATH = "/referral";

    @Autowired
    private ReferralCodeService referralCodeService;

    @GetMapping("/list")
    public PaginatedResponse<ReferralCodeDto> getList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return referralCodeService.getList(pageNo, pageSize);
    }

    @GetMapping
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

    @GetMapping("/activate/{referralCode}")
    public void activate(@PathVariable("referralCode") String referralCode){
        referralCodeService.activate(referralCode);
    }

    @GetMapping("/deactivate/{referralCode}")
    public void deActivate(@PathVariable("referralCode") String referralCode ){
        referralCodeService.deActivate(referralCode);
    }

    @GetMapping("/referral/stats")
    public ReferralStatsDto getStats(){
        return referralCodeService.getStats();
    }

}
