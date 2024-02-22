package com.nasnav.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.ReferralCodeRepo;
import com.nasnav.dao.ReferralSettingsRepo;
import com.nasnav.dao.ReferralTransactionRepository;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeCreateResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralStatsDto;
import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.MobileOTPService;
import com.nasnav.integration.smsMis.dto.OTPDto;
import com.nasnav.mappers.ReferralCodeMapper;
import com.nasnav.persistence.*;
import com.nasnav.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.math.RoundingMode.FLOOR;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ReferralCodeServiceImpl implements ReferralCodeService {

    private static final Logger logger = LogManager.getLogger("ReferralCodeService");

    @Autowired
    private ObjectMapper objectMapper;

    private final ReferralCodeMapper referralCodeMapper;

    private final ReferralCodeRepo referralCodeRepo;

    private final OrganizationService organizationService;

    private final UserService userService;

    private final SecurityService securityService;

    private final ReferralWalletService referralWalletService;

    private final MobileOTPService mobileOTPService;

    private final ReferralSettingsRepo referralSettingsRepo;

    private final ReferralTransactionRepository referralTransactionRepo;

    @Override
    public ReferralCodeDto getForUser() {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        Long userId = securityService.getCurrentUser().getId();
        return referralCodeMapper.map(
                referralCodeRepo.findByUser_IdAndOrganization_Id(userId, currentOrganizationId)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0008))
        );
    }
    //TODO get wallet amount(earned - Share revenue)
    //number of user registered with referral
    // order discounts total

    @Override
    public ReferralCodeDto get(String referralCode) {
       return referralCodeMapper.map(referralCodeRepo.findByReferralCode(referralCode)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode))
               );
    }

    public PaginatedResponse<ReferralCodeDto> getList(int pageNo, int pageSize){
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

        return referralCodeMapper.map(
                referralCodeRepo.findAllByOrganization_id(currentOrganizationId, pageable)
        );
    }

    public void send(String phoneNumber, String parentReferralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        UserEntity user = (UserEntity) securityService.getCurrentUser();

       if(referralCodeRepo.existsByUser_IdAndOrganization_Id(user.getId(), currentOrganizationId)){
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0009);
       }

        ReferralCodeEntity existParentReferralCodeEntity = null;
        if(parentReferralCode != null && !parentReferralCode.isEmpty()) {
            existParentReferralCodeEntity= referralCodeRepo.findByReferralCodeAndOrganization_id(parentReferralCode, currentOrganizationId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, parentReferralCode));
        }
       String referralToken = generateReferralCodeToken();
       ReferralCodeEntity referralCodeEntity = new ReferralCodeEntity();
       referralCodeEntity.setUser(user);
       referralCodeEntity.setOrganization(organizationService.getOrganizationById(currentOrganizationId));
       referralCodeEntity.setReferralCode(generateReferralCode());
       referralCodeEntity.setAcceptReferralToken(referralToken);
       referralCodeEntity.setSettings(referralSettingsRepo.findByOrganization_Id(currentOrganizationId).get());
       referralCodeEntity.setStatus(ReferralCodeStatus.IN_ACTIVE.getValue());
       if(existParentReferralCodeEntity != null){
           referralCodeEntity.setParentReferralCode(existParentReferralCodeEntity.getReferralCode());
       }
        String  responseStatus = mobileOTPService.send(new OTPDto(phoneNumber, referralToken, "Your Referral OTP: " + referralToken));
        if(!responseStatus.equals("Success")) {
           throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0006);
        }
        referralCodeRepo.save(referralCodeEntity);
    }


    @Override
    public void update(ReferralCodeDto referralCodeDto) {
        ReferralCodeEntity existReferralCodeEntity = referralCodeRepo.findById(referralCodeDto.getId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0002, referralCodeDto.getId()));
      referralCodeRepo.save(referralCodeMapper.map(referralCodeDto, existReferralCodeEntity));
    }

    @Override
    public void activate(String referralCode) {
        ReferralCodeEntity existReferralCode =  referralCodeRepo.findByReferralCode(referralCode)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));

        existReferralCode.setStatus(ReferralCodeStatus.ACTIVE.getValue());

        referralCodeRepo.save(existReferralCode);
    }

    @Override
    public void deActivate(String referralCode) {
        ReferralCodeEntity existReferralCode =  referralCodeRepo.findByReferralCode(referralCode)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));

        existReferralCode.setStatus(ReferralCodeStatus.IN_ACTIVE.getValue());

        referralCodeRepo.save(existReferralCode);
    }

    @Override
    public void delete(String referralCode) {

    }

    private String generateReferralCode(){
        String referralCode = generate(6);
        while(referralCodeRepo.existsByReferralCode(referralCode)){
            referralCode = generate(6);
        }
        return referralCode;
    }

    private String generateReferralCodeToken(){
        String referralCodeToken = generate(8);
        while(referralCodeRepo.existsByAcceptReferralToken(referralCodeToken)){
            referralCodeToken = generate(8);
        }
        return referralCodeToken;
    }

    private String generate(int codeLength){
        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        System.out.println(output);
        return output ;
    }

    @Override
    @Transactional
    public ReferralCodeDto validateReferralOtp(String referralOtpToken) {
       ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByAcceptReferralToken(referralOtpToken)
               .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0003));
       if(!securityService.getCurrentUser().getId().equals(referralCodeEntity.getUser().getId())) {
           throw new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0007);
       }
       if(!referralCodeEntity.getAcceptReferralToken().equals(referralOtpToken)) {
            throw new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0005);
       }
       referralCodeEntity.setStatus(ReferralCodeStatus.ACTIVE.getValue());
       referralCodeRepo.save(referralCodeEntity);
       BigDecimal openingValue = readConfigJsonStr(referralCodeEntity.getSettings().getConstraints()).get(ReferralCodeType.REFERRAL_ACCEPT_REVENUE);
       referralWalletService.create(referralCodeEntity, openingValue);

       return  referralCodeMapper.map(referralCodeEntity);
    }

    public void addReferralDiscountForSubOrders(String referralCode, Set<OrdersEntity> subOrders, Long userId) {
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndStatus(referralCode, ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
        if(!userId.equals(existingReferralCode.getUser().getId())){
            return;
        }
        BigDecimal oderDiscountPercentage = readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
        subOrders.forEach(s -> {
                    s.setDiscounts(
                            s.getDiscounts().add(s.getSubTotal().multiply(oderDiscountPercentage).setScale(2, FLOOR)));
                    s.setAppliedReferralCode(referralCode);
                }
        );
    }

    public BigDecimal shareRevenueForOrder(OrdersEntity ordersEntity){
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndStatus(ordersEntity.getAppliedReferralCode(), ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));

        ReferralCodeEntity parentReferralCode  = null;
        BigDecimal shareRevenueAmount = new BigDecimal("0.0");
        if(existingReferralCode.getParentReferralCode() != null && !existingReferralCode.getParentReferralCode().isEmpty()) {
          parentReferralCode  = referralCodeRepo.findByReferralCodeAndStatus(existingReferralCode.getParentReferralCode(), ReferralCodeStatus.ACTIVE.getValue())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
            BigDecimal shareRevenuePercentage = readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(ReferralCodeType.SHARE_REVENUE_PERCENTAGE);
            shareRevenueAmount = ordersEntity.getSubTotal().multiply(shareRevenuePercentage).setScale(2, RoundingMode.FLOOR);
            referralWalletService.deposit(ordersEntity.getId(), shareRevenueAmount, parentReferralCode, ReferralTransactionsType.ORDER_SHARE_REVENUE);
        }
        return shareRevenueAmount;
    }

    @Override
    public BigDecimal calculateTheWithdrawValueFromReferralBalance(Long userId, BigDecimal orderAmount){
        ReferralWallet referralWallet = referralWalletService.getWalletByUserId(userId);
        if(referralWallet.getBalance().compareTo(orderAmount) > 0){
            return orderAmount;
        }
        return referralWallet.getBalance();
    }
    @Override
    public void withDrawFromReferralWallet(MetaOrderEntity order){
        referralWalletService.withdraw(order.getUser(), order.getId(),
                order.getReferralWithdrawAmount(), ReferralTransactionsType.ORDER_WITHDRAWAL);
    }


    public BigDecimal getReferralConfigValue(String referralCode, ReferralCodeType type) {
        ReferralCodeEntity existingReferralCode = referralCodeRepo.findByReferralCode(referralCode)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
       return readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(type);
    }

    public Long saveReferralTransactionForOrderDiscount(OrdersEntity ordersEntity) {
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCode(ordersEntity.getAppliedReferralCode())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
        BigDecimal referralDiscountPercentage = getReferralConfigValue(ordersEntity.getAppliedReferralCode(), ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
        BigDecimal discountValue =  ordersEntity.getSubTotal().multiply(referralDiscountPercentage).setScale(2, RoundingMode.FLOOR);
        return referralWalletService.addReferralTransaction(ordersEntity.getMetaOrder().getUser(), discountValue, ordersEntity.getId(), existingReferralCode,
                ReferralTransactionsType.ORDER_DISCOUNT, false);
    }

    @Override
    public ReferralStatsDto getStats(){
        Long currentUserId = securityService.getCurrentUser().getId();
        return ReferralStatsDto.builder()
                .totalEarningFromReferral(referralTransactionRepo.sumAmountByTypeAndUser_Id(ReferralTransactionsType.ORDER_SHARE_REVENUE.name(), currentUserId))
                .numberOfReferred(referralCodeRepo.countChildReferralCodesByUserIdAndIsActive(currentUserId, ReferralCodeStatus.ACTIVE.getValue()))
                .build();
    }

    public String generateRandomToken(){
        return UUID.randomUUID().toString();
    }


    private HashMap<ReferralCodeType, BigDecimal> readConfigJsonStr(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<HashMap<ReferralCodeType, BigDecimal>>() {
            });
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0001, jsonStr);
        }
    }




}
