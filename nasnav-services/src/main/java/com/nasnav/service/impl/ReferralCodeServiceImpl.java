package com.nasnav.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.nasnav.dao.ReferralCodeRepo;
import com.nasnav.dao.ReferralSettingsRepo;
import com.nasnav.dao.ReferralTransactionRepository;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.dto.referral_code.ReferralConstraints;
import com.nasnav.dto.referral_code.ReferralStatsDto;
import com.nasnav.dto.referral_code.ReferralTransactionsDto;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.MobileOTPService;
import com.nasnav.integration.smsmisr.dto.OTPDto;
import com.nasnav.mappers.ReferralCodeMapper;
import com.nasnav.persistence.*;
import com.nasnav.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ReferralCodeServiceImpl implements ReferralCodeService {

    private static final Logger logger = LogManager.getLogger("ReferralCodeService");

    private final ObjectMapper objectMapper;

    private final ReferralCodeMapper referralCodeMapper;

    private final ReferralCodeRepo referralCodeRepo;

    private final OrganizationService organizationService;

    private final SecurityService securityService;

    private final ReferralWalletService referralWalletService;

    private final MobileOTPService mobileOTPService;

    private final ReferralSettingsRepo referralSettingsRepo;

    private final ReferralTransactionRepository referralTransactionRepo;

    private final UserService userService;

    @Override
    public ReferralCodeDto getForUser() {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        Long userId = securityService.getCurrentUser().getId();
        ReferralCodeDto referralCodeDto = referralCodeMapper.map(referralCodeRepo.findByUserIdAndReferralTypeAndOrganizationId(userId, ReferralType.USER, currentOrganizationId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0008)));
        referralCodeDto.setUsername(userService.getUsernameById(userId));
        return referralCodeDto;
    }

    @Override
    public ReferralCodeDto get(String referralCode) {
        ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByReferralCodeAndReferralType(referralCode, ReferralType.USER)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0003, referralCode));

        ReferralCodeDto referralCodeDto = referralCodeMapper.map(referralCodeEntity);
        referralCodeDto.setUsername(userService.getUsernameById(referralCodeEntity.getUserId()));
       return referralCodeDto;
    }

    public PaginatedResponse<ReferralCodeDto> getList(int pageNo, int pageSize){
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

        return referralCodeMapper.map(
                referralCodeRepo.findAllByReferralTypeAndOrganizationId(ReferralType.USER, currentOrganizationId, pageable)
        );
    }

    @Override
    public PaginatedResponse<ReferralTransactionsDto> getChilds(ReferralTransactionsType referralTransactionsType, String dateFrom, String dateTo, int pageNo, int pageSize){
        Long currentUserId = securityService.getCurrentUser().getId();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<ReferralTransactions> result;
        if(dateFrom != null && !dateFrom.isEmpty() && dateTo != null && !dateTo.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime dateTimeFrom =  LocalDateTime.of(LocalDate.parse(dateFrom, formatter), LocalTime.MIDNIGHT);
            LocalDateTime dateTimeTo =  LocalDateTime.of(LocalDate.parse(dateTo, formatter), LocalTime.MAX);
            result = referralTransactionRepo.getChildsReferralsByTransactionTypeAndReferralType(currentUserId, referralTransactionsType, ReferralType.USER, dateTimeFrom, dateTimeTo, pageable);
        } else {
            result = referralTransactionRepo.getChildsReferralsByTransactionTypeAndReferralType(currentUserId, referralTransactionsType, ReferralType.USER, pageable);
        }

        return PaginatedResponse.<ReferralTransactionsDto>builder()
                .totalRecords(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .content(result.getContent()
                        .stream().map(ref -> ReferralTransactionsDto.builder()
                                        .no(ref.getId())
                                .activities(getActivityMessageByType(ref, referralTransactionsType))
                                .createdAt(ref.getCreatedAt())
                                .amount(ref.getAmount())
                                .build())
                        .collect(Collectors.toList())
                )
                .build();

    }

    @Override
    public String  getActivityMessageByType(ReferralTransactions referralTransactions, ReferralTransactionsType type) {
        if(type.equals(ReferralTransactionsType.ACCEPT_REFERRAL_CODE)) {
            return userService.getUsernameById(referralTransactions.getUserId())+ " User Registered with your referral";
        }
        if(type.equals(ReferralTransactionsType.ORDER_SHARE_REVENUE)) {
            Long userId = referralCodeRepo.findByReferralCodeAndReferralType(referralTransactions.getReferralCodeEntity().getReferralCode(), ReferralType.USER).get()
                    .getUserId();
            return "You award share revenue from " + userService.getUsernameById(userId) + " order" ;
        }
        return "";
    }

    public void send(String phoneNumber, String parentReferralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        UserEntity user = (UserEntity) securityService.getCurrentUser();

        validateReferralRegistration(phoneNumber, parentReferralCode);

        ReferralCodeEntity existParentReferralCodeEntity = null;
        if(StringUtils.isNotEmpty(parentReferralCode)) {
            existParentReferralCodeEntity= referralCodeRepo.findByReferralCodeAndReferralTypeAndOrganizationId(parentReferralCode, ReferralType.USER, currentOrganizationId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, parentReferralCode));
        }


        ReferralCodeEntity referralCodeEntity =
                referralCodeRepo.findByUserIdAndReferralTypeAndOrganizationIdAndStatus(user.getId(), ReferralType.USER, currentOrganizationId, ReferralCodeStatus.IN_ACTIVE.getValue())
                        .orElse(null);

        if(referralCodeEntity != null && !referralCodeEntity.getPhoneNumber().equals(phoneNumber)) {
            referralCodeEntity.setPhoneNumber(phoneNumber);
            String referralOtp = generateReferralCodeToken();
            referralCodeEntity.setAcceptReferralToken(referralOtp);
        } else {
            referralCodeEntity = new ReferralCodeEntity();
            String referralOtp = generateReferralCodeToken();
            referralCodeEntity.setUserId(user.getId());
            referralCodeEntity.setOrganization(organizationService.getOrganizationById(currentOrganizationId));
            referralCodeEntity.setReferralCode(generateReferralCode());
            referralCodeEntity.setPhoneNumber(phoneNumber);
            referralCodeEntity.setAcceptReferralToken(referralOtp);
            referralCodeEntity.setSettings(referralSettingsRepo.findByReferralTypeAndOrganizationId(ReferralType.USER, currentOrganizationId).orElse(null));
            referralCodeEntity.setStatus(ReferralCodeStatus.IN_ACTIVE.getValue());
            referralCodeEntity.setReferralType(ReferralType.USER);
            if(existParentReferralCodeEntity != null){
                referralCodeEntity.setParentReferralCode(existParentReferralCodeEntity.getReferralCode());
            }

            String responseStatus = mobileOTPService.send(new OTPDto(phoneNumber, referralOtp));
            if(!responseStatus.equals("Success")) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0006);
            }
        }
        referralCodeRepo.save(referralCodeEntity);
    }

    @Override
    public void resend() {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        UserEntity user = (UserEntity) securityService.getCurrentUser();
        ReferralCodeEntity referralCodeEntity =
                referralCodeRepo.findByUserIdAndReferralTypeAndOrganizationIdAndStatus(user.getId(), ReferralType.USER, currentOrganizationId, ReferralCodeStatus.IN_ACTIVE.getValue())
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0008));
        String responseStatus = mobileOTPService.send(new OTPDto(referralCodeEntity.getPhoneNumber(), referralCodeEntity.getAcceptReferralToken()));
        if(!responseStatus.equals("Success")) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0006);
        }
    }

    public void validateReferralRegistration(String phoneNumber, String parentReferralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        UserEntity user = (UserEntity) securityService.getCurrentUser();

        if(referralCodeRepo.existsByUserIdAndReferralTypeAndOrganizationIdAndStatus(user.getId(), ReferralType.USER, currentOrganizationId, ReferralCodeStatus.ACTIVE.getValue())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0014);
        }

        ReferralSettings referralSettings = referralSettingsRepo.findByReferralTypeAndOrganizationId(ReferralType.USER, currentOrganizationId)
                .orElseThrow( () -> new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0010));

        if(StringUtils.isEmpty(parentReferralCode)) {
            ReferralConstraints parentReferralConstraints = readConfigJsonStr(referralSettings.getConstraints()).get(ReferralCodeType.PARENT_REGISTRATION);
            if (!checkDateIntervalValidity(LocalDate.now(), parentReferralConstraints.getValidFrom(), parentReferralConstraints.getValidTo())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0011);
            }
        }
        ReferralConstraints childReferralConstraints = readConfigJsonStr(referralSettings.getConstraints()).get(ReferralCodeType.CHILD_REGISTRATION);
        if(!checkDateIntervalValidity(LocalDate.now(), childReferralConstraints.getValidFrom(), childReferralConstraints.getValidTo())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0012);
        }

        validateReferralPhoneNumberRegistration(phoneNumber);

    }

    public void validateReferralPhoneNumberRegistration(String phoneNUmber) {
        if(referralCodeRepo.existsByPhoneNumber(phoneNUmber)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0013);
        }
    }

    @Override
    public void activate(String referralCode) {
        ReferralCodeEntity existReferralCode =  referralCodeRepo.findByReferralCodeAndReferralType(referralCode, ReferralType.USER)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));

        existReferralCode.setStatus(ReferralCodeStatus.ACTIVE.getValue());

        referralCodeRepo.save(existReferralCode);
    }

    @Override
    public void deActivate(String referralCode) {
        ReferralCodeEntity existReferralCode =  referralCodeRepo.findByReferralCodeAndReferralType(referralCode, ReferralType.USER)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));

        existReferralCode.setStatus(ReferralCodeStatus.IN_ACTIVE.getValue());

        referralCodeRepo.save(existReferralCode);
    }

    private String generateReferralCode(){
        String referralCode = generate(6);
        while(referralCodeRepo.existsByReferralCodeAndReferralType(referralCode, ReferralType.USER)){
            referralCode = generate(6);
        }
        return referralCode;
    }

    @Override
    public String generateReferralCodeToken(){
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
        return sb.toString();
    }

    @Override
    @Transactional
    public ReferralCodeDto validateReferralOtp(String referralOtpToken) {
        Long currentUserId = securityService.getCurrentUser().getId();
        Long currentOrganizationId= securityService.getCurrentUserOrganizationId();
       ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByUserIdAndReferralTypeAndOrganizationId(currentUserId, ReferralType.USER, currentOrganizationId)
               .orElseThrow(() ->  new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0007));

       if(!referralCodeEntity.getAcceptReferralToken().equals(referralOtpToken)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0005);
       }
       referralCodeEntity.setStatus(ReferralCodeStatus.ACTIVE.getValue());
       referralCodeRepo.save(referralCodeEntity);
       ReferralConstraints referralDiscountConstraints = readConfigJsonStr(referralCodeEntity.getSettings().getConstraints()).get(ReferralCodeType.REFERRAL_ACCEPT_REVENUE);
       referralWalletService.create(referralCodeEntity, referralDiscountConstraints.getValue());
       userService.updateUserPhone(currentUserId, currentOrganizationId, referralCodeEntity.getPhoneNumber());
       return  referralCodeMapper.map(referralCodeEntity);
    }

    public void addReferralDiscountForSubOrders(String referralCode, Set<OrdersEntity> subOrders, Long userId) {
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndReferralTypeAndStatus(referralCode, ReferralType.USER, ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
        if(!userId.equals(existingReferralCode.getUserId())){
            return;
        }

        ReferralConstraints referralConstraints = readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
        if(checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())){
            subOrders.forEach(s -> {
                        s.setDiscounts(
                                s.getDiscounts().add(s.getSubTotal().multiply(referralConstraints.getValue()).setScale(2, FLOOR)));
                        s.setAppliedReferralCode(referralCode);
                    }
            );
        }
    }

    @Override
    public boolean checkIntervalDateForCurrentOrganization(ReferralCodeType referralCodeType){
        ReferralSettings referralSettings = referralSettingsRepo.
                findByReferralTypeAndOrganizationId(ReferralType.USER, securityService.getCurrentUserOrganizationId())
                .orElse(null);
        if(Objects.isNull(referralSettings)) {
            return false;
        }
        ReferralConstraints referralConstraints = readConfigJsonStr(referralSettings.getConstraints()).get(referralCodeType);
        return checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo());
    }


    public boolean checkDateIntervalValidity(LocalDate currentDate, LocalDate from, LocalDate to) {
        return (from.isBefore(currentDate) || from.isEqual(currentDate))
                && (to.isAfter(currentDate) || to.isEqual(currentDate));
    }


    @Override
    public BigDecimal calculateReferralDiscountForCartItems(String referralCode, List<CartItem> items, Long userId) {
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndReferralTypeAndStatus(referralCode, ReferralType.USER, ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
        if(!userId.equals(existingReferralCode.getUserId())){
            return BigDecimal.ZERO;
        }
        ReferralConstraints referralConstraints = readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
        if(!checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())){
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map( item -> {
                         BigDecimal referralDiscount = ofNullable(item.getDiscount()).orElse(ZERO).add(
                            item.getPrice()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                                    .multiply(referralConstraints.getValue()).setScale(2, FLOOR));
                    item.setDiscount(referralDiscount);
                    return referralDiscount;
                })
                .reduce(BigDecimal.ZERO, (init, aggregated) ->  init.add(aggregated));
    }

    public BigDecimal shareRevenueForOrder(OrdersEntity ordersEntity){
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndReferralTypeAndStatus(ordersEntity.getAppliedReferralCode(), ReferralType.USER, ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
        ReferralCodeEntity parentReferralCode  = null;
        BigDecimal shareRevenueAmount = new BigDecimal("0.0");
        if(existingReferralCode.getParentReferralCode() != null && !existingReferralCode.getParentReferralCode().isEmpty()) {
          parentReferralCode  = referralCodeRepo.findByReferralCodeAndReferralTypeAndStatus(existingReferralCode.getParentReferralCode(), ReferralType.USER, ReferralCodeStatus.ACTIVE.getValue())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
            ReferralConstraints referralConstraints = readConfigJsonStr(parentReferralCode.getSettings().getConstraints())
                    .get(ReferralCodeType.SHARE_REVENUE_PERCENTAGE);
            if(!checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())) {
                return shareRevenueAmount;
            }
            shareRevenueAmount = (ordersEntity.getSubTotal().subtract(ordersEntity.getDiscounts())).multiply(referralConstraints.getValue()).setScale(2, RoundingMode.DOWN);
            referralWalletService.deposit(ordersEntity.getId(), shareRevenueAmount, parentReferralCode, existingReferralCode, ReferralTransactionsType.ORDER_SHARE_REVENUE);
        }
        return shareRevenueAmount;
    }

    @Override
    public BigDecimal calculateTheWithdrawValueFromReferralBalance(Long userId, BigDecimal orderAmount){
        ReferralWallet referralWallet = referralWalletService.getWalletByUserId(userId);
        if(referralWallet.getBalance().compareTo(orderAmount) < 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0015);
        }
        return referralWallet.getBalance().min(orderAmount);
    }
    @Override
    public boolean withDrawFromReferralWallet(MetaOrderEntity order){
       ReferralSettings referralSettings = referralSettingsRepo.findByReferralTypeAndOrganizationId(ReferralType.USER, securityService.getCurrentUserOrganizationId()).orElse(null);
       if(referralSettings == null) {
           return false;
       }
        referralWalletService.withdraw(order.getUser(), order.getId(),
                order.getReferralWithdrawAmount(), ReferralTransactionsType.ORDER_WITHDRAWAL);
        return true;
    }


    public ReferralConstraints getReferralConfigValue(String referralCode, ReferralCodeType type) {
        ReferralCodeEntity existingReferralCode = referralCodeRepo.findByReferralCodeAndReferralType(referralCode, ReferralType.USER)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
       return readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(type);
    }

    public void saveReferralTransactionForOrderDiscount(OrdersEntity ordersEntity) {
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndReferralType(ordersEntity.getAppliedReferralCode(), ReferralType.USER)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
        ReferralConstraints referralConstraints = getReferralConfigValue(ordersEntity.getAppliedReferralCode(), ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
        if(checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())) {
            BigDecimal discountValue = ordersEntity.getSubTotal().multiply(referralConstraints.getValue()).setScale(2, RoundingMode.FLOOR);
            referralWalletService.addReferralTransaction(ordersEntity.getMetaOrder().getUser(), discountValue, ordersEntity.getId(), existingReferralCode,
                    ReferralTransactionsType.ORDER_DISCOUNT);
        }
    }

    public  BigDecimal calculatePayWithReferralOnOrders(Set<OrdersEntity> subOrders, Long userId, BigDecimal discounts, BigDecimal total, BigDecimal subTotal) {
        BigDecimal referralBalanceWithdraw = calculateTheWithdrawValueFromReferralBalance(userId, total.subtract(discounts));
        if(referralBalanceWithdraw.compareTo(ZERO) > 0) {
            for(OrdersEntity order : subOrders) {
                BigDecimal discountToApply = order.getTotal().subtract(order.getDiscounts());
                order.setDiscounts(order.getDiscounts().add(discountToApply));
                order.setReferralWithdrawAmount(discountToApply);
                order.setTotal(order.getTotal().subtract(discountToApply));
            }
        }
        return referralBalanceWithdraw;
    }

    public void returnWithdrawAmountToUserReferralWallet(MetaOrderEntity metaOrder) {
        if(Objects.nonNull(metaOrder.getReferralWithdrawAmount())
                && metaOrder.getReferralWithdrawAmount().compareTo(ZERO) > 0) {
            referralWalletService.deposit(metaOrder.getId(), metaOrder.getReferralWithdrawAmount(),
                    metaOrder.getUser().getId(), ReferralTransactionsType.ORDER_CANCELLED);

        }
    }

    @Override
    public ReferralStatsDto getStats(){
        Long currentUserId = securityService.getCurrentUser().getId();
        return ReferralStatsDto.builder()
                .shareRevenueEarningsFromChildReferrals(referralTransactionRepo.sumAmountByTypeAndUserIdAndReferralType(ReferralTransactionsType.ORDER_SHARE_REVENUE, currentUserId, ReferralType.USER))
                .numberOfActiveChildReferrals(referralCodeRepo.countChildReferralCodesByUserIdAndReferralTypeAndIsActive(currentUserId, ReferralType.USER.toString(), ReferralCodeStatus.ACTIVE.getValue()))
                .orderDiscountsAwarded(referralTransactionRepo.sumAmountByTypeAndUserIdAndReferralType(ReferralTransactionsType.ORDER_DISCOUNT, currentUserId, ReferralType.USER))
                .walletBalance(referralWalletService.getWalletByUserId(currentUserId).getBalance())
                .build();
    }

    private Map<ReferralCodeType, ReferralConstraints> readConfigJsonStr(String jsonStr) {
        try {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(jsonStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0001, jsonStr);
        }
    }

}
