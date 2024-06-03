package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.InfluencerReferralConstraints;
import com.nasnav.dto.referral_code.InfluencerReferralDto;
import com.nasnav.dto.referral_code.InfluencerReferralItemDto;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.InfluencerReferralMapper;
import com.nasnav.persistence.*;
import com.nasnav.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.nasnav.exceptions.ErrorCodes.*;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.*;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class InfluencerReferralServiceImpl implements InfluencerReferralService {

    private final InfluencerReferralRepository influencerReferralRepo;

    private final InfluencerReferralMapper influencerReferralMapper;

    private final SecurityService securityService ;

    private final OrganizationService organizationService;

    @Autowired
    @Qualifier("influencerReferralWalletServiceImpl")
    private ReferralWalletService referralWalletService;

    private final PasswordEncoder passwordEncoder;

    private final ReferralSettingsRepo referralSettingsRepo;

    private final ReferralCodeRepo referralCodeRepo;

    private final PromotionRepository promotionRepository;

    @Override
    public InfluencerReferralDto register(InfluencerReferralDto influencerReferralDTO) {
        if(promotionRepository.existsByCode(influencerReferralDTO.getReferralCode())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, INFREF$005);
        }

        if(influencerReferralRepo.existsByUserName(influencerReferralDTO.getUserName())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, INFREF$001);
        }

        if(!isPasswordConfirmed(influencerReferralDTO.getPassword(), influencerReferralDTO.getConfirmPassword())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, INFREF$002);
        }

        InfluencerReferral influencerReferral = influencerReferralMapper.map(influencerReferralDTO);

        influencerReferralRepo.save(influencerReferral);

        OrganizationEntity organizationEntity = organizationService.getOrganizationById(securityService.getCurrentUserOrganizationId());

        ReferralSettings referralSettings = new ReferralSettings();
        referralSettings.setOrganization(organizationEntity);
        referralSettings.setReferralType(ReferralType.INFLUENCER);
        referralSettings.setConstraints("{}");
        referralSettings.setName("Influencer " + influencerReferral.getFirstName() + " " + influencerReferral.getLastName());
        referralSettingsRepo.save(referralSettings);

        ReferralCodeEntity referralCodeEntity = new ReferralCodeEntity();
        referralCodeEntity.setReferralType(ReferralType.INFLUENCER);
        referralCodeEntity.setReferralCode(influencerReferralDTO.getReferralCode());
        referralCodeEntity.setSettings(referralSettings);
        referralCodeEntity.setOrganization(organizationEntity);
        referralCodeEntity.setUserId(influencerReferral.getId());
        referralCodeEntity.setStatus(ReferralCodeStatus.IN_ACTIVE.getValue());
        referralCodeRepo.save(referralCodeEntity);

        ReferralWallet referralWallet = referralWalletService.create(referralCodeEntity, BigDecimal.ZERO);

        influencerReferral.setReferralSettings(referralSettings);
        influencerReferral.setReferral(referralCodeEntity);
        influencerReferral.setReferralWallet(referralWallet);
        influencerReferralRepo.save(influencerReferral);

        return influencerReferralMapper.map(influencerReferral);
    }

    private boolean isPasswordConfirmed(String password, String confirmPassword) {
        return password.equalsIgnoreCase(confirmPassword);
    }

    @Override
    public InfluencerReferralDto getWalletBalance(String userName, String password) {
        InfluencerReferral influencerReferral = influencerReferralRepo.findByUserName(userName)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, INFREF$003));

        if (!passwordEncoder.matches(password, influencerReferral.getPassword())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, INFREF$003);
        }
        return influencerReferralMapper.map(influencerReferral);
    }

    public void updateReferralSettings(String username,  InfluencerReferralConstraints constraints) throws JsonProcessingException {
        if(Objects.nonNull(constraints.getCashbackPercentage()) && constraints.getCashbackPercentage().compareTo(BigDecimal.ZERO) > 0
          && Objects.nonNull(constraints.getCashbackValue()) && constraints.getCashbackValue().compareTo(BigDecimal.ZERO) > 0 ) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, INFREF$006);
        }
        InfluencerReferral influencerReferral = influencerReferralRepo.findByUserName(username)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, INFREF$004));
        ReferralSettings referralSettings = influencerReferral.getReferralSettings();
        referralSettings.setConstraints(writeConfigJsonStr(constraints));
        referralSettingsRepo.save(referralSettings);
    }

    @Override
    public PaginatedResponse<InfluencerReferralDto> getAllInfluencerReferrals(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<InfluencerReferral> pageResult = influencerReferralRepo.findAll(pageable);
        return PaginatedResponse.<InfluencerReferralDto>builder()
                .totalPages(pageResult.getTotalPages())
                .totalRecords(pageResult.getTotalElements())
                .content(influencerReferralMapper.map(pageResult.getContent()))
                .build();
    }


    /**
     * Calculate and set the discount on each suborder if its products are in allowed products
     * on referral settings products list
     *
     * @param influencerReferralCode
     * @param subOrders
     * @return
     */
    @Override
    public BigDecimal calculateDiscount(String influencerReferralCode, Set<OrdersEntity> subOrders) {
        InfluencerReferral influencerReferral = influencerReferralRepo.findByReferralReferralCode(influencerReferralCode)
                .orElse(null);
        if (Objects.isNull(influencerReferral)) {
            return BigDecimal.ZERO;
        }
        InfluencerReferralConstraints constraints = readConfigJsonStr(influencerReferral
                .getReferralSettings().getConstraints());
        if (!isConstraintsDateInRange(constraints)) {
            return BigDecimal.ZERO;
        }
        var allowedProductIds = constraints.getProducts();
        var referralItemsDto = mapInfluencerReferralItems(subOrders);
        var applicableItems = referralItemsDto
                .stream()
                .filter(i -> allowedProductIds.contains(i.getProductId()))
                .toList();
        var subtotal = applicableItems.stream().map(this::calcTotalValue).reduce(ZERO, BigDecimal::add);
        var discount = getDiscountFromConstraints(constraints, subtotal);
        setDiscountForSuborders(applicableItems, subOrders, discount, subtotal, influencerReferralCode);
        return discount;
    }


    /**
     * Calculate total discount cart item if its products are in allowed products
     * on referral settings products list
     *
     * @param influencerReferralCode
     * @param cart
     * @return
     */
    @Override
    public BigDecimal calculateDiscountForCart(String influencerReferralCode, Cart cart) {
        InfluencerReferral influencerReferral = influencerReferralRepo.findByReferralReferralCode(influencerReferralCode)
                .orElse(null);
        if (Objects.isNull(influencerReferral)) {
            return BigDecimal.ZERO;
        }
        InfluencerReferralConstraints constraints = readConfigJsonStr(influencerReferral
                .getReferralSettings().getConstraints());
        if (!isConstraintsDateInRange(constraints)) {
            return BigDecimal.ZERO;
        }
        var allowedProductIds = constraints.getProducts();
        var referralItemsDto = mapInfluencerReferralItemsFromCart(cart);
        var applicableItems = referralItemsDto
                .stream()
                .filter(i -> allowedProductIds.contains(i.getProductId()))
                .toList();
        var subtotal = applicableItems.stream().map(this::calcTotalValue).reduce(ZERO, BigDecimal::add);
        return  getDiscountFromConstraints(constraints, subtotal);
    }

    private boolean isConstraintsDateInRange(InfluencerReferralConstraints constraints) {
        return constraints.getStartDate().isEqual(LocalDate.now()) || constraints.getStartDate().isBefore(LocalDate.now())
                && constraints.getEndDate().isEqual(LocalDate.now()) || constraints.getEndDate().isAfter(LocalDate.now());
    }

    private BigDecimal getDiscountFromConstraints(InfluencerReferralConstraints constraints, BigDecimal subtotal) {
        if(Objects.nonNull(constraints.getDiscountPercentage()) &&
                constraints.getDiscountPercentage().compareTo(ZERO) > 0) {
            return subtotal.multiply(constraints.getDiscountPercentage());
        }
        return Objects.nonNull(constraints.getDiscountValue())?
                constraints.getDiscountValue() : ZERO;
    }

    private void setDiscountForSuborders(List<InfluencerReferralItemDto> applicableItems, Set<OrdersEntity> subOrders, BigDecimal referralDiscount, BigDecimal subTotal, String referralCode) {
        List<OrdersEntity> appliedOrderDiscounts = subOrders.stream()
                .filter(suborder -> suborder.getBasketsEntity().stream()
                        .map(BasketsEntity::getStocksEntity)
                        .map(StocksEntity::getProductVariantsEntity)
                        .map(ProductVariantsEntity::getProductEntity)
                        .map(ProductEntity::getId)
                        .anyMatch(applicableItems.stream().map(InfluencerReferralItemDto::getProductId).toList()::contains))
                .toList();

               appliedOrderDiscounts.forEach( subOrder -> {
                    addReferralDiscountOnSuborders(referralDiscount,  subOrder, subTotal);
                    adjustDiscountErrorCalculation(referralDiscount, appliedOrderDiscounts);
                    subOrder.setAppliedInfluencerReferralCode(referralCode);
                });
    }

    private void adjustDiscountErrorCalculation(BigDecimal referralDiscount, List<OrdersEntity> appliedOrderDiscounts) {
        BigDecimal calculatedPromotionDiscount = appliedOrderDiscounts.stream()
                .map(OrdersEntity::getDiscounts)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal calculationError = referralDiscount.subtract(calculatedPromotionDiscount);

        appliedOrderDiscounts
                .stream()
                .findFirst()
                .ifPresent(subOrder -> subOrder.setDiscounts(subOrder.getDiscounts().add(calculationError)));
    }

    private void addReferralDiscountOnSuborders(BigDecimal referralDiscount, OrdersEntity subOrder
            , BigDecimal subTotal) {
        BigDecimal proportion = subOrder.getSubTotal().divide(subTotal, 2, FLOOR);
        BigDecimal subOrderReferralDiscount = proportion.multiply(referralDiscount).setScale(2, FLOOR);
        subOrder.setDiscounts(subOrder.getDiscounts().add(subOrderReferralDiscount));
   }

    private BigDecimal calcTotalValue(InfluencerReferralItemDto item) {
        var price = ofNullable(item.getPrice()).orElse(ZERO);
        var discount = ofNullable(item.getDiscount()).orElse(ZERO);
        var qty = ofNullable(item.getQuantity()).map(Object::toString).map(BigDecimal::new).orElse(ZERO);
        return price.subtract(discount).multiply(qty);
    }

    private List<InfluencerReferralItemDto> mapInfluencerReferralItems(Set<OrdersEntity> subOrders) {
        return subOrders
                .stream()
                .map(OrdersEntity::getBasketsEntity)
                .flatMap(Set::stream)
                .map(this::mapInfluencerReferralItemDto)
                .toList();
    }

    private List<InfluencerReferralItemDto> mapInfluencerReferralItemsFromCart(Cart cart) {
        return cart.getItems()
                .stream()
                .map(this::mapCartItemToInfluencerReferralItem)
                .toList();
    }
    
    
    private InfluencerReferralItemDto mapCartItemToInfluencerReferralItem(CartItem items) {
        InfluencerReferralItemDto influencerReferralItemDto = new InfluencerReferralItemDto();
        influencerReferralItemDto.setDiscount(items.getDiscount());
        influencerReferralItemDto.setPrice(items.getPrice());
        influencerReferralItemDto.setProductId(items.getProductId());
        influencerReferralItemDto.setQuantity(items.getQuantity());
        return influencerReferralItemDto;
    }
    private InfluencerReferralItemDto mapInfluencerReferralItemDto(BasketsEntity basket) {
        var influencerReferralItemDto = new InfluencerReferralItemDto();
        influencerReferralItemDto.setPrice(basket.getPrice());
        influencerReferralItemDto.setDiscount(basket.getDiscount());

        var stock = ofNullable(basket).map(BasketsEntity::getStocksEntity);
        var variant = stock.map(StocksEntity::getProductVariantsEntity);
        var product = variant.map(ProductVariantsEntity::getProductEntity);

        ofNullable(basket.getQuantity()).map(BigDecimal::intValue).ifPresent(influencerReferralItemDto::setQuantity);
        product.map(ProductEntity::getId).ifPresent(influencerReferralItemDto::setProductId);
        return influencerReferralItemDto;
    }


    /**
     * deposit to the influencer wallet an amount if order has an applied influnecer referral discount
     * the amount that will be deposited is on influencer referral settings
     * @param ordersEntity
     */
    @Override
    public void addInfluencerCashback(OrdersEntity ordersEntity) {
        if(StringUtils.isBlankOrNull(ordersEntity.getAppliedInfluencerReferralCode())){
            return;
        }

        InfluencerReferral influencerReferral = influencerReferralRepo.findByReferralReferralCode(ordersEntity.getAppliedInfluencerReferralCode())
                .orElse(null);
        if (Objects.isNull(influencerReferral)) {
            return;
        }

        InfluencerReferralConstraints constraints = readConfigJsonStr(influencerReferral
                .getReferralSettings().getConstraints());
        BigDecimal influencerCashBack = Objects.nonNull(constraints.getCashbackPercentage()) &&
                constraints.getCashbackPercentage().compareTo(ZERO) > 0 ?
                (ordersEntity.getSubTotal().subtract(ordersEntity.getDiscounts())).multiply(constraints.getCashbackPercentage()).setScale(2, RoundingMode.DOWN)
                :constraints.getCashbackValue();
        referralWalletService.deposit(ordersEntity.getId(), influencerCashBack, influencerReferral.getReferral(),
                influencerReferral.getReferral(), ReferralTransactionsType.INFLUENCER_CASHBACK);

    }

    public String writeConfigJsonStr(InfluencerReferralConstraints constraints) {
        try {
            return getObjectMapper().writeValueAsString(constraints);
        } catch (Exception e) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0002);
        }
    }

    public InfluencerReferralConstraints readConfigJsonStr(String jsonStr) {
        try {
            return getObjectMapper().readValue(jsonStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0001, jsonStr);
        }
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }


}
