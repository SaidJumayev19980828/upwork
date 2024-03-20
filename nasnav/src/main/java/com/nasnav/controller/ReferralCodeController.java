package com.nasnav.controller;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.dto.referral_code.ReferralStatsDto;
import com.nasnav.dto.referral_code.ReferralTransactionsDto;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.service.ReferralCodeService;
import com.nasnav.service.ReferralSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
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

    @GetMapping(value = "/organization/settings")
    public ReferralSettingsDto getSettings(@RequestHeader(TOKEN_HEADER) String userToken){
        return referralSettingsService.get();
    }

    @PostMapping(value = "/organization/settings", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public ReferralSettingsDto createSettings(@RequestHeader(TOKEN_HEADER) String userToken,
                                              @RequestBody ReferralSettingsDto referralSettingsDto){
        return referralSettingsService.create(referralSettingsDto);
    }

    @PutMapping(value = "/organization/settings", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public void updateSettings(@RequestHeader(TOKEN_HEADER) String userToken,
                               @RequestBody ReferralSettingsDto referralSettingsDto){
         referralSettingsService.update(referralSettingsDto);
    }

    @GetMapping("/settings/discount_percentage")
    public Map<ReferralCodeType, BigDecimal> createSettings(@RequestHeader(TOKEN_HEADER) String userToken){
        return referralSettingsService.getValue(ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
    }

    @GetMapping("/list")
    public PaginatedResponse<ReferralCodeDto> getList(@RequestHeader(TOKEN_HEADER) String userToken,
                                                      @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return referralCodeService.getList(pageNo, pageSize);
    }

    @GetMapping("/childs")
    public PaginatedResponse<ReferralTransactionsDto> getChilds(@RequestHeader(TOKEN_HEADER) String userToken,
                                                      @RequestParam(value = "type") ReferralTransactionsType type,
                                                      @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                                      @RequestParam(value = "dateTo", required = false) String  dateTo,
                                                      @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return referralCodeService.getChilds(type, dateFrom, dateTo, pageNo, pageSize);
    }


    @GetMapping("/user")
    public ReferralCodeDto get(@RequestHeader(TOKEN_HEADER) String userToken){
        return referralCodeService.getForUser();
    }

    @GetMapping("/code/{code}")
    public ReferralCodeDto get(@RequestHeader(TOKEN_HEADER) String userToken,
                                @PathVariable("code") String code){
        return referralCodeService.get(code);
    }


    @PostMapping("/sendOtp")
    public void send(@RequestHeader(TOKEN_HEADER) String userToken,
                     @RequestParam("phoneNumber") String phoneNumber,
                     @RequestParam(value = "parentReferralCode", required = false) String parentReferralCode) {
         referralCodeService.send(phoneNumber, parentReferralCode);
    }

    @PostMapping("/resendOtp")
    public void send(@RequestHeader(TOKEN_HEADER) String userToken) {
        referralCodeService.resend();
    }

    @PostMapping("/validateOtp/{referral_token}")
    public ReferralCodeDto validate(@RequestHeader(TOKEN_HEADER) String userToken,
                                    @PathVariable("referral_token") String token) {
        return referralCodeService.validateReferralOtp(token);
    }

    @PostMapping("/activate/{referralCode}")
    public void activate(@RequestHeader(TOKEN_HEADER) String userToken,
                         @PathVariable("referralCode") String referralCode){
        referralCodeService.activate(referralCode);
    }

    @PostMapping("/deactivate/{referralCode}")
    public void deActivate(@RequestHeader(TOKEN_HEADER) String userToken,
                           @PathVariable("referralCode") String referralCode ){
        referralCodeService.deActivate(referralCode);
    }


    @GetMapping("/stats")
    public ReferralStatsDto getStats(@RequestHeader(TOKEN_HEADER) String userToken){
        return referralCodeService.getStats();
    }

}
