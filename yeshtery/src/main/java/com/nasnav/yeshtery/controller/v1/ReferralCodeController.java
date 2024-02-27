package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.service.ReferralCodeService;
import com.nasnav.service.ReferralSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping(ReferralCodeController.API_PATH)
@CrossOrigin("*")
public class ReferralCodeController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/referral";

    @Autowired
    private ReferralCodeService referralCodeService;

    @Autowired
    private ReferralSettingsService referralSettingsService;

    @PostMapping("/organization/settings")
    public ReferralSettingsDto createSettings(@RequestBody ReferralSettingsDto referralSettingsDto){
        return referralSettingsService.create(referralSettingsDto);
    }

    @PostMapping("/sendOtp")
    public void send(@RequestParam("phoneNumber") String phoneNumber,
                     @RequestParam(value = "parentReferralCode", required = false) String parenReferralCode) {
        referralCodeService.send(phoneNumber, parenReferralCode);
    }

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


    @PostMapping("/accept/{referralCode}")
    public void accept(@PathVariable String referralCode, @RequestParam("referral_token") String token) {
         referralCodeService.validateReferralOtp(token);

    }

    @GetMapping("/activate/{referralCode}")
    public void activate(@PathVariable("referralCode") String referralCode){
        referralCodeService.activate(referralCode);
    }

    @GetMapping("/deactivate/{referralCode}")
    public void deActivate(@PathVariable("referralCode") String referralCode ){
        referralCodeService.deActivate(referralCode);
    }

}
