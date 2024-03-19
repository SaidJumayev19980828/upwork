package com.nasnav.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
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
import com.nasnav.exceptions.BusinessException;
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
        return referralCodeMapper.map(
                referralCodeRepo.findByUser_IdAndOrganization_Id(userId, currentOrganizationId)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0008))
        );
    }

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

    @Override
    public PaginatedResponse<ReferralTransactionsDto> getChilds(ReferralTransactionsType referralTransactionsType, String dateFrom, String dateTo, int pageNo, int pageSize){
        Long currentUserId = securityService.getCurrentUser().getId();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<ReferralTransactions> result;
        if(dateFrom != null && !dateFrom.isEmpty() && dateTo != null && !dateTo.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime dateTimeFrom =  LocalDateTime.of(LocalDate.parse(dateFrom, formatter), LocalTime.MIDNIGHT);
            LocalDateTime dateTimeTo =  LocalDateTime.of(LocalDate.parse(dateTo, formatter), LocalTime.MAX);
            result = referralTransactionRepo.getChildsReferralsByTransactionType(currentUserId, referralTransactionsType, dateTimeFrom, dateTimeTo, pageable);
        } else {
            result = referralTransactionRepo.getChildsReferralsByTransactionType(currentUserId, referralTransactionsType, pageable);
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

    public String getActivityMessageByType(ReferralTransactions referralTransactions, ReferralTransactionsType type) {
        if(type.equals(ReferralTransactionsType.ACCEPT_REFERRAL_CODE)) {
            return referralTransactions.getUser().getName() + " User Registered with your referral";
        }
        if(type.equals(ReferralTransactionsType.ORDER_SHARE_REVENUE)) {
            String username = referralCodeRepo.findByReferralCode(referralTransactions.getReferralCodeEntity().getReferralCode()).get()
                    .getUser().getName();
            return "You award share revenue from " + username + " order" ;
        }
        return "";
    }

    public void send(String phoneNumber, String parentReferralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        UserEntity user = (UserEntity) securityService.getCurrentUser();

        validateReferralRegistration(phoneNumber, parentReferralCode);

        ReferralCodeEntity existParentReferralCodeEntity = null;
        if(StringUtils.isNotEmpty(parentReferralCode)) {
            existParentReferralCodeEntity= referralCodeRepo.findByReferralCodeAndOrganization_id(parentReferralCode, currentOrganizationId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, parentReferralCode));
        }


        ReferralCodeEntity referralCodeEntity =
                referralCodeRepo.findByUser_IdAndOrganization_IdAndStatus(user.getId(), currentOrganizationId, ReferralCodeStatus.IN_ACTIVE.getValue())
                        .orElse(null);

        if(referralCodeEntity != null && !referralCodeEntity.getPhoneNumber().equals(phoneNumber)) {
            referralCodeEntity.setPhoneNumber(phoneNumber);
            String referralOtp = generateReferralCodeToken();
            referralCodeEntity.setAcceptReferralToken(referralOtp);
        } else {
            referralCodeEntity = new ReferralCodeEntity();
            String referralOtp = generateReferralCodeToken();
            referralCodeEntity.setUser(user);
            referralCodeEntity.setOrganization(organizationService.getOrganizationById(currentOrganizationId));
            referralCodeEntity.setReferralCode(generateReferralCode());
            referralCodeEntity.setPhoneNumber(phoneNumber);
            referralCodeEntity.setAcceptReferralToken(referralOtp);
            referralCodeEntity.setSettings(referralSettingsRepo.findByOrganization_Id(currentOrganizationId).get());
            referralCodeEntity.setStatus(ReferralCodeStatus.IN_ACTIVE.getValue());
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


    public void validateReferralRegistration(String phoneNumber, String parentReferralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        UserEntity user = (UserEntity) securityService.getCurrentUser();

        if(referralCodeRepo.existsByUser_IdAndOrganization_IdAndStatus(user.getId(), currentOrganizationId, ReferralCodeStatus.ACTIVE.getValue())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0014);
        }

        ReferralSettings referralSettings = referralSettingsRepo.findByOrganization_Id(currentOrganizationId)
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
        Long currentUserId = securityService.getCurrentUser().getId();
        Long currentOrganizationId= securityService.getCurrentUserOrganizationId();
       ReferralCodeEntity referralCodeEntity = referralCodeRepo.findByUser_IdAndOrganization_Id(currentUserId, currentOrganizationId)
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
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndStatus(referralCode, ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
        if(!userId.equals(existingReferralCode.getUser().getId())){
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
        ReferralSettings referralSettings = referralSettingsRepo.findByOrganization_Id(securityService.getCurrentUserOrganizationId()).orElse(null);
        if(Objects.isNull(referralSettings)) {
            return false;
        }
        ReferralConstraints referralConstraints = readConfigJsonStr(referralSettings.getConstraints()).get(referralCodeType);
        return checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo());
    }


    public boolean checkDateIntervalValidity(LocalDate currentDate, LocalDate from, LocalDate to) {
        return (from.isBefore(currentDate) && to.isAfter(currentDate))
                || (from.isEqual(currentDate) || to.isEqual(currentDate));
    }


    @Override
    public BigDecimal calculateReferralDiscountForCartItems(String referralCode, List<CartItem> items, Long userId) {
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndStatus(referralCode, ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
        if(!userId.equals(existingReferralCode.getUser().getId())){
            return BigDecimal.ZERO;
        }
        ReferralConstraints referralConstraints = readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
        if(!checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())){
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map( item -> {
                         BigDecimal referralDiscount = item.getDiscount().add(
                            item.getPrice()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                                    .multiply(referralConstraints.getValue()).setScale(2, FLOOR));
                    item.setDiscount(referralDiscount);
                    return referralDiscount;
                })
                .reduce(BigDecimal.ZERO, (init, aggregated) ->  init.add(aggregated));
    }


    public BigDecimal shareRevenueForOrder(OrdersEntity ordersEntity){
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCodeAndStatus(ordersEntity.getAppliedReferralCode(), ReferralCodeStatus.ACTIVE.getValue())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
        ReferralConstraints referralConstraints = readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(ReferralCodeType.SHARE_REVENUE_PERCENTAGE);
        if(!checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())){
            return BigDecimal.ZERO;
        }
        ReferralCodeEntity parentReferralCode  = null;
        BigDecimal shareRevenueAmount = new BigDecimal("0.0");
        if(existingReferralCode.getParentReferralCode() != null && !existingReferralCode.getParentReferralCode().isEmpty()) {
          parentReferralCode  = referralCodeRepo.findByReferralCodeAndStatus(existingReferralCode.getParentReferralCode(), ReferralCodeStatus.ACTIVE.getValue())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
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
    public void withDrawFromReferralWallet(MetaOrderEntity order){
        ReferralSettings referralSettings = referralSettingsRepo.findByOrganization_Id(securityService.getCurrentUserOrganizationId()).orElse(null);
       if(referralSettings == null) {
           return;
       }
        ReferralConstraints referralConstraints = readConfigJsonStr(referralSettings.getConstraints()).get(ReferralCodeType.PAY_WITH_REFERRAL_WALLET);
        if(!checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())) {
            return;
        }
        referralWalletService.withdraw(order.getUser(), order.getId(),
                order.getReferralWithdrawAmount(), ReferralTransactionsType.ORDER_WITHDRAWAL);
    }


    public ReferralConstraints getReferralConfigValue(String referralCode, ReferralCodeType type) {
        ReferralCodeEntity existingReferralCode = referralCodeRepo.findByReferralCode(referralCode)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));
       return readConfigJsonStr(existingReferralCode.getSettings().getConstraints()).get(type);
    }

    public void saveReferralTransactionForOrderDiscount(OrdersEntity ordersEntity) {
        ReferralCodeEntity existingReferralCode =  referralCodeRepo.findByReferralCode(ordersEntity.getAppliedReferralCode())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, ordersEntity.getAppliedReferralCode()));
        ReferralConstraints referralConstraints = getReferralConfigValue(ordersEntity.getAppliedReferralCode(), ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE);
        if(checkDateIntervalValidity(LocalDate.now(), referralConstraints.getValidFrom(), referralConstraints.getValidTo())) {
            BigDecimal discountValue = ordersEntity.getSubTotal().multiply(referralConstraints.getValue()).setScale(2, RoundingMode.FLOOR);
            referralWalletService.addReferralTransaction(ordersEntity.getMetaOrder().getUser(), discountValue, ordersEntity.getId(), existingReferralCode,
                    ReferralTransactionsType.ORDER_DISCOUNT, false);
        }
    }

    public  BigDecimal calculatePayWithReferralOnOrders(Set<OrdersEntity> subOrders, Long userId, BigDecimal discounts, BigDecimal total, BigDecimal subTotal) {
        BigDecimal referralBalanceWithdraw = calculateTheWithdrawValueFromReferralBalance(userId, total.subtract(discounts));
        if(referralBalanceWithdraw.compareTo(ZERO) > 0) {
            for(OrdersEntity order : subOrders) {
                BigDecimal discountToApply = order.getTotal().subtract(order.getDiscounts());
                order.setDiscounts(order.getDiscounts().add(discountToApply));
                order.setReferralWithdrawAmount(discountToApply);
            }
        }
        return referralBalanceWithdraw;
    }


    @Override
    public ReferralStatsDto getStats(){
        Long currentUserId = securityService.getCurrentUser().getId();
        return ReferralStatsDto.builder()
                .shareRevenueEarningsFromChildReferrals(referralTransactionRepo.sumAmountByTypeAndUser_Id(ReferralTransactionsType.ORDER_SHARE_REVENUE, currentUserId))
                .numberOfActiveChildReferrals(referralCodeRepo.countChildReferralCodesByUserIdAndIsActive(currentUserId, ReferralCodeStatus.ACTIVE.getValue()))
                .orderDiscountsAwarded(referralTransactionRepo.sumAmountByTypeAndUser_Id(ReferralTransactionsType.ORDER_DISCOUNT, currentUserId))
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
