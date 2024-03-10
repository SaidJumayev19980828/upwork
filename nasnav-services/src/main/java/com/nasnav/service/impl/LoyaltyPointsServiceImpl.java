package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.enumerations.LoyaltyTransactions;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.OrganizationPoints;
import com.nasnav.response.LoyaltyPointDeleteResponse;
import com.nasnav.response.LoyaltyPointsUpdateResponse;
import com.nasnav.response.LoyaltyUserPointsResponse;
import com.nasnav.service.LoyaltyPointsService;
import com.nasnav.service.LoyaltyTierService;
import com.nasnav.service.SecurityService;

import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.enumerations.LoyaltyPointType.*;
import static com.nasnav.enumerations.LoyaltyTransactions.ORDER_ONLINE;
import static com.nasnav.enumerations.LoyaltyTransactions.REFERRAL;
import static com.nasnav.enumerations.LoyaltyTransactions.SHARE_POINTS;
import static com.nasnav.enumerations.LoyaltyTransactions.TRANSFER_POINTS;
import static com.nasnav.enumerations.LoyaltyTransactions.PICKUP_FROM_SHOP;

import static com.nasnav.enumerations.OrderStatus.DELIVERED;
import static com.nasnav.enumerations.Settings.RETURN_DAYS_LIMIT;
import static com.nasnav.enumerations.ShippingStatus.PICKED_UP;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.impl.OrderReturnServiceImpl.MAX_RETURN_TIME_WINDOW;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class LoyaltyPointsServiceImpl implements LoyaltyPointsService {
    private static final Logger logger = LogManager.getLogger("LoyaltyPointsService");
    private final AppConfig config;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private LoyaltyTierService loyaltyTierService;

    @Autowired
    private LoyaltyPointTransactionRepository loyaltyPointTransRepo;

    @Autowired
    private LoyaltySpendTransactionRepository loyaltySpendTransactionRepo;

    @Autowired
    private LoyaltyPointConfigRepository loyaltyPointConfigRepo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ShopsRepository shopsRepo;
    @Autowired
    private CartItemRepository cartItemRepo;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private OrdersRepository ordersRepo;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private SettingRepository settingRepo;
    @Autowired
    private LoyaltyPinsRepository loyaltyPinsRepository;

    @Autowired
    private LoyaltyTierRepository loyaltyTierRepository;

    @Autowired
    private UserLoyaltyPointsRepository  userLoyaltyPointsRepository;

    @Autowired
    private UserLoyaltyTransactionsRepository loyaltyTransactionsRepository;
    @Override
    public LoyaltyPointsUpdateResponse updateLoyaltyPointConfig(LoyaltyPointConfigDTO dto) {
        validateLoyaltyPointConfigDTO(dto);

        LoyaltyPointConfigEntity entity = prepareLoyaltyPointConfigEntity(dto);

        updateUsersTiers(entity.getDefaultTier());

        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

    private void updateUsersTiers(LoyaltyTierEntity tier) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        userRepo.updateUsersTiers(tier.getId(), orgId);
    }

    private LoyaltyPointConfigEntity prepareLoyaltyPointConfigEntity(LoyaltyPointConfigDTO dto) {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        LoyaltyPointConfigEntity entity = new LoyaltyPointConfigEntity();
        if (dto.getId() != null) {
            entity = loyaltyPointConfigRepo.findByIdAndOrganization_IdAndIsActive(dto.getId(), org.getId(), TRUE)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0018, dto.getId(), org.getId()));

            if (dto.getDefaultTier() != null && dto.getDefaultTier().getId() != null) {
                setConfigDefaultTier(dto.getDefaultTier().getId(), org.getId(), entity);
            }
        } else {
            //Solve bug not to inActivate other Config
            //loyaltyPointConfigRepo.setAllOrgConfigsAsInactive(org.getId());
            Optional<LoyaltyPointConfigEntity> defaultTierActiveConfig = loyaltyPointConfigRepo.findByDefaultTier_IdAndIsActive(dto.getDefaultTier().getId(), true);
            if (defaultTierActiveConfig.isPresent()){
                throw new RuntimeBusinessException(NOT_ACCEPTABLE,ORG$LOY$0026,dto.getDefaultTier().getId(),defaultTierActiveConfig.get().getId());
            }

            entity.setIsActive(true);
            entity.setOrganization(org);
            setConfigDefaultTier(dto.getDefaultTier().getId(), org.getId(), entity);
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getConstraints() != null && !dto.getConstraints().isEmpty()) {
            for (Map.Entry<LoyaltyPointType, LoyaltyConfigConstraint> e : dto.getConstraints().entrySet()) {
                var value = e.getValue();
                if (anyIsNull(value.getRatioFrom(), value.getRatioTo())) {
                    throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0008);
                }
            }
            entity.setConstraints(serializeDTO(dto.getConstraints()));
        }

        return loyaltyPointConfigRepo.save(entity);
    }

    private String serializeDTO(Map<LoyaltyPointType, LoyaltyConfigConstraint> dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            logger.error(e, e);
            return "{}";
        }
    }

    private void setConfigDefaultTier(Long tierId, Long orgId, LoyaltyPointConfigEntity entity) {
        LoyaltyTierEntity tier = loyaltyTierRepository.findByIdAndOrganization_Id(tierId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0019, tierId));
        entity.setDefaultTier(tier);
    }

    private void validateLoyaltyPointConfigDTO(LoyaltyPointConfigDTO dto) {
        if (dto.getId() == null) {
            if (anyIsNull(dto, dto.getConstraints(), dto.getDescription(),
                    dto.getDefaultTier(), dto.getDefaultTier().getId()))
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0008);
        }
    }

    @Override
    public LoyaltyPointDeleteResponse deleteLoyaltyPointConfig(Long id) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        LoyaltyPointConfigEntity entity = loyaltyPointConfigRepo.findByIdAndOrganization_IdAndIsActive(id, orgId, TRUE)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0011, id));
        entity.setIsActive(false);
        loyaltyPointConfigRepo.save(entity);
        return new LoyaltyPointDeleteResponse(true, entity.getId());
    }

    @Deprecated
    @Override
    public LoyaltyUserPointsResponse getUserPoints() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return getUserPoints(orgId);
    }
    @Override
    public LoyaltyUserPointsResponse loyaltyUserPoints() {
        UserEntity user = getCurrentUserWithOrganization();
        return new LoyaltyUserPointsResponse(getUserLoyaltyPointsBalance(user));
    }

    @Override
    public LoyaltyUserPointsResponse loyaltyUserPoints(Long orgId) {
        UserEntity user = getCurrentUserWithOrg(orgId);
        return new LoyaltyUserPointsResponse(getUserLoyaltyPointsBalance(user));
    }

    private UserEntity getCurrentUserWithOrganization() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return getCurrentUserWithOrg(orgId);
    }

    private BigDecimal getUserLoyaltyPointsBalance(UserEntity user) {
        return getUserLoyaltyPoints(user).map(UserLoyaltyPoints::getBalance).orElse(ZERO);
    }

    private  Optional<UserLoyaltyPoints>  getUserLoyaltyPoints(UserEntity user) {
       return userLoyaltyPointsRepository.findByUser(user);
    }



    @Deprecated
    @Override
    public LoyaltyUserPointsResponse getUserPoints(Long orgId) {
        UserEntity user = getCurrentUserWithOrg(orgId);
        List<LoyaltyPointTransactionEntity> userTrx = loyaltyPointTransRepo.findByUser_IdAndOrganization_IdAndStartDateBeforeAndIsValidAndTypeLessThan(user.getId(),orgId,LocalDateTime.now(),true, SPEND_IN_ORDER.getValue());
        Integer totalPoints = 0;
        for(LoyaltyPointTransactionEntity pt : userTrx){
            totalPoints+=pt.getPoints().intValue();
            List<LoyaltySpentTransactionEntity> allReverseTrx = loyaltySpendTransactionRepo.findAllByTransaction_Id(pt.getId());
            for(LoyaltySpentTransactionEntity negativePt : allReverseTrx){
                totalPoints -= negativePt.getReverseTransaction().getPoints().intValue();
            }
        }
        return new LoyaltyUserPointsResponse(ZERO);
    }

    @Override
    public List<OrganizationPoints> getUserPointsPerOrg() {
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if (!(baseUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity currentUser = (UserEntity) baseUser;
        return loyaltyPointTransRepo.findRedeemablePointsPerOrg(currentUser.getYeshteryUserId());
    }

    private UserEntity getCurrentUserWithOrg(Long orgId) {
        BaseUserEntity baseUser = null;

        try {
            baseUser = securityService.getCurrentUserForOrg(orgId);
        } catch (IllegalStateException e) {
            throw new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0014, orgId);
        }

        if (!(baseUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }

        return (UserEntity) baseUser;
    }

    @Override
    public LoyaltyTierDTO getUserOrgTier(Long orgId) {
        UserEntity user = getCurrentUserWithOrg(orgId);

        return getLoyaltyTierDTO(user);
    }

    @Override
    public String generateUserShopPinCode(Long shopId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        ShopsEntity shop = shopsRepo.findByIdAndOrganizationEntity_IdAndRemoved(shopId, orgId, 0)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0005, shopId, orgId));

        SecureRandom random = new SecureRandom();
        int num = random.nextInt(100000);
        String formattedPin = format("%05d", num);

        LoyaltyPinsEntity pinsEntity = new LoyaltyPinsEntity();
        pinsEntity.setShop(shop);
        pinsEntity.setPin(formattedPin);
        loyaltyPinsRepository.save(pinsEntity);
        return formattedPin;
    }

    private LoyaltyTierDTO getLoyaltyTierDTO(UserEntity userEntity) {
        return ofNullable(userEntity.getTier())
                .map(entity -> {
                    LoyaltyTierDTO dto = entity.getRepresentation();
                    dto.setConstraints(loyaltyTierService.readTierJson(entity.getConstraints()));
                    return dto;
                }).
                orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0021, userEntity.getId()));
    }

    @Deprecated
    @Override
    public LoyaltyPointsUpdateResponse createLoyaltyPointTransaction(ShopsEntity shop, OrganizationEntity org,
                                                                     UserEntity user,
                                                                     MetaOrderEntity yeshteryMetaOrder,
                                                                     OrdersEntity order, BigDecimal points,
                                                                     BigDecimal amount, Integer expiry) {
        LoyaltyPointTransactionEntity entity = createLoyaltyPointTransaction(org, user, points, amount, expiry);
        entity.setShop(shop);
        entity.setIsValid(true);
        entity.setUser(user);
        entity.setOrder(order);
        entity.setMetaOrder(yeshteryMetaOrder);
        entity.setType(null);
        loyaltyPointTransRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }
    private LoyaltyPointsUpdateResponse addLoyaltyPointTransaction(ShopsEntity shop, OrganizationEntity org,
                                                                     UserEntity user,
                                                                     MetaOrderEntity yeshteryMetaOrder,
                                                                     OrdersEntity order, BigDecimal points) {

       UserLoyaltyPoints userPoints =  processTransaction(user, points, ORDER_ONLINE,org,shop,order,yeshteryMetaOrder);
        userLoyaltyPointsRepository.save(userPoints);
        return new LoyaltyPointsUpdateResponse(userPoints.getId());
    }

    private UserLoyaltyPoints buildBasicEntity(UserEntity user, BigDecimal amount){
        UserLoyaltyPoints  entity = new UserLoyaltyPoints();
        entity.depositPoints(amount);
        entity.setUser(user);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
    @Deprecated
    private LoyaltyPointTransactionEntity createLoyaltyPointTransaction(OrganizationEntity org,
                                                                        UserEntity user,
                                                                        BigDecimal points,
                                                                        BigDecimal amount,
                                                                        Integer expiry) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setAmount(amount);
        entity.setIsValid(false);
        entity.setUser(user);
        entity.setOrganization(org);
        entity.setStartDate(calculateTransactionStartDate(org));
        entity.setEndDate(calculateTransactionEndDate(entity.getStartDate(), expiry));
        return entity;
    }

    private UserLoyaltyTransactions addLoyaltyPointTransaction(OrganizationEntity org,BigDecimal points) {
        UserLoyaltyTransactions entity = new UserLoyaltyTransactions();
        entity.setAmount(points);
        entity.setOrganization(org);
        entity.setCreatedAt(calculateTransactionStartDate(org));
        return entity;
    }
    private LocalDateTime calculateTransactionStartDate(OrganizationEntity org) {
        int returnExpirySetting = settingRepo.findBySettingNameAndOrganization_Id(RETURN_DAYS_LIMIT.name(), org.getId())
                .map(SettingEntity::getSettingValue)
                .map(Integer::parseInt)
                .orElse(MAX_RETURN_TIME_WINDOW);
        return LocalDateTime.now().plusDays(returnExpirySetting);
    }

    private LocalDateTime calculateTransactionEndDate(LocalDateTime startDate, Integer expiry) {
        if (expiry == null) {
            return null;
        }
        LocalDateTime startDateCopy = startDate;
        return startDateCopy.plusDays(expiry);
    }

    @Override
    public void createLoyaltyPointTransaction(OrdersEntity order, LoyaltyPointType type, BigDecimal pointsAmount) {
        OrganizationEntity org = order.getOrganizationEntity();
        ShopsEntity shop = order.getShopsEntity();

        Optional<UserEntity> userEntityOp = userRepo.findById(order.getUserId());
        if (userEntityOp.isEmpty()) {
            return;
        }
        UserEntity userEntity = userEntityOp.get();
        if (userEntity.getTier() == null) {
            return;
        }

        LoyaltyPointConfigEntity config =loyaltyPointConfigRepo.findActiveConfigTierByTierId(userEntity.getTier().getId()).orElse(null);
        if (config == null) {
            return;
        }

        BigDecimal points = calculatePoints(config, userEntity.getTier(), pointsAmount, ORDER_ONLINE);
        addLoyaltyPointTransaction(shop, org, userEntity, null, order, points);
    }

    public BigDecimal getTierCoefficientByType(LoyaltyTierEntity entity, LoyaltyTransactions type) {
        return loyaltyTierService.readTierJson(entity.getConstraints()).get(type);
    }

    @Override
    public void createYeshteryLoyaltyPointTransaction(MetaOrderEntity yeshteryMetaOrder, LoyaltyPointType type, BigDecimal pointsAmount) {
        OrganizationEntity org = yeshteryMetaOrder.getOrganization();
        UserEntity user = yeshteryMetaOrder.getUser();
        if(user.getTier() == null) {
            return;
        }
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId(user.getTier().getId()).orElse(null);
        if (config == null) {
            return;
        }
        BigDecimal points = calculatePoints(config, user.getTier(), pointsAmount, ORDER_ONLINE);
        addLoyaltyPointTransaction(null, org, user, yeshteryMetaOrder, null, points);
    }

    public BigDecimal calculatePoints(LoyaltyPointConfigEntity config, LoyaltyTierEntity tier, BigDecimal amount, LoyaltyTransactions type) {
        BigDecimal coefficient = getTierCoefficientByType(tier, type);

        LoyaltyConfigConstraint constraint = getConfigConstraint(config, type);
        BigDecimal from = ofNullable(constraint.getRatioFrom()).orElse(ZERO);
        BigDecimal to = ofNullable(constraint.getRatioTo()).orElse(ZERO);
        BigDecimal localAmount = ofNullable(amount).orElse(constraint.getAmount());

        if (anyIsNull(from, to, coefficient, localAmount)) {
            logger.warn(ORG$LOY$0002.getValue());
            return BigDecimal.ZERO;
        }
//        return localAmount.multiply(coefficient).multiply(from).divide(to, 2, RoundingMode.HALF_EVEN); based on some discussion with frontend devs that was invalid
        return localAmount.multiply(coefficient);
    }

    @Override
    @Transactional
    public void redeemPoints(Long orderId, String code) {
        LoyaltyPinsEntity pinEntity = loyaltyPinsRepository.findByPin(code)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0017, code));
        Long shopId = pinEntity.getShop().getId();
        UserEntity user = (UserEntity) securityService.getCurrentUser();
        OrdersEntity order = ordersRepo.findPickUpOrderByOrderIdUserIdAndShopId(orderId, user.getId(), shopId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, O$CFRM$0001, shopId, orderId));
        OrganizationEntity org = order.getOrganizationEntity();

        order.getShipment().setStatus(PICKED_UP.getValue());
        order.setStatus(DELIVERED.getValue());

        ordersRepo.save(order);

        prepareLoyaltyPointTransaction(user, org, PICKUP_FROM_SHOP, order.getTotal(), true);
        loyaltyPinsRepository.delete(pinEntity);
        activateReferralPoints(order);
    }

    private void prepareLoyaltyPointTransaction(UserEntity user, OrganizationEntity org, LoyaltyTransactions type, BigDecimal amount, Boolean valid) {
        if (user.getTier() == null) {
            return;
        }
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId(user.getTier().getId())
                .orElse(null);
        if (config == null) {
            return;
        }
        BigDecimal points = calculatePoints(config, user.getTier(), amount, type);
        userLoyaltyPointsRepository.save(processTransaction(user,points, type,org,null,null,null));
    }



    @Override
    public List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints(Long orgId) {
        UserEntity user = getCurrentUserWithOrg(orgId);
        return listOrganizationLoyaltyPoints(user.getId(), orgId);
    }

    private List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints(Long userId, Long orgId) {
        return loyaltyTransactionsRepository.findByUser_IdAndOrganization_Id(userId, orgId)
                .stream()
                .map(UserLoyaltyTransactions::getRepresentation)
                .collect(toList());
    }



    @Override
    public List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPointsByUser(Long userId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (config.isYeshteryInstance) {
            Set<Long> userIds = userRepo.findByYeshteryUserIdOfUserId(userId).stream().map(UserEntity::getId)
                    .collect(Collectors.toSet());
            return loyaltyPointTransRepo.findByUserIdInAndOrganizationId(userIds, orgId)
                    .stream()
                    .map(LoyaltyPointTransactionEntity::getRepresentation)
                    .collect(toList());
        } else {
            return listOrganizationLoyaltyPoints(userId, orgId);
        }
    }

    @Override
    public List<LoyaltyPointConfigDTO> listLoyaltyPointConfigs() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return loyaltyPointConfigRepo.findByOrganization_IdOrderByCreatedAtDesc(orgId)
                .stream()
                .map(entity -> {
                    LoyaltyPointConfigDTO dto = entity.getRepresentation();
                    dto.setConstraints(readConfigJsonStr(entity.getConstraints()));
                    return dto;
                })
                .collect(toList());
    }

    @Override
    public List<LoyaltyPointConfigDTO> listLoyaltyPointConfigsForAllOrganizations() {
        return loyaltyPointConfigRepo.findByIsActiveAndOrganizationYeshteryStateOrderByCreatedAtDesc(true, 1)
                .stream()
                .map(entity -> {
                    LoyaltyPointConfigDTO dto = entity.getRepresentation();
                    dto.setConstraints(readConfigJsonStr(entity.getConstraints()));
                    return dto;
                })
                .collect(toList());
    }

    @Override
    public LoyaltyPointConfigDTO getLoyaltyPointActiveConfig() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(orgId, true)
                .map(e -> {
                    LoyaltyPointConfigDTO dto = e.getRepresentation();
                    dto.setConstraints(readConfigJsonStr(e.getConstraints()));
                    return dto;
                })
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0024, orgId));
    }

    @Override
    public  SpentPointsInfo applyPointsOnOrders(BigDecimal points , Set<OrdersEntity> subOrders, BigDecimal totalWithoutShipping, Long userId, OrganizationEntity org){
        UserEntity user = userRepo.findById(userId).get();
        if(user.getTier() == null) {
            return new SpentPointsInfo();
        }
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId( user.getTier().getId())
                .orElse(null);
        if (config == null) {
            return new SpentPointsInfo();
        }

        BigDecimal total =
                calculatePointsDiscountAndCreateSpendTransactions(
                        config,
                        totalWithoutShipping,
                        points)
                        .stream()
                        .map(AppliedPoints::getDiscount)
                        .reduce(ZERO, BigDecimal::add);

        BigDecimal suborderPointsDiscount = total.divide(new BigDecimal(subOrders.size()));
        subOrders.forEach(s -> s.setDiscounts(s.getDiscounts().add(suborderPointsDiscount)));
        return new SpentPointsInfo();
    }

    @Override
    public SpentPointsInfo applyPointsOnOrders(Set<Long> points,
                                               Set<OrdersEntity> subOrders,
                                               BigDecimal totalWithoutShipping,
                                               Long userId,
                                               OrganizationEntity org) {
        UserEntity user = userRepo.findById(userId).get();
        if(user.getTier() == null) {
            return new SpentPointsInfo();
        }
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId( user.getTier().getId())
                .orElse(null);
        if (config == null) {
            return new SpentPointsInfo();
        }

        List<LoyaltyPointTransactionEntity> earnedPoints = loyaltyPointTransRepo.getTransactionsByIdInAndUserIdAndOrgId(points, userId, org.getId());
        List<LoyaltyPointTransactionEntity> spentPoints = new ArrayList<>();
        List<LoyaltySpentTransactionEntity> spentPointsRef = new ArrayList<>();

        BigDecimal total =
                calculatePointsDiscountAndCreateSpendTransactions(
                        config,
                        totalWithoutShipping,
                        user,
                        org,
                        earnedPoints,
                        spentPoints,
                        spentPointsRef)
                        .stream()
                        .map(AppliedPoints::getDiscount)
                        .reduce(ZERO, BigDecimal::add);

        BigDecimal suborderPointsDiscount = total.divide(new BigDecimal(subOrders.size()));
        subOrders.forEach(s -> s.setDiscounts(s.getDiscounts().add(suborderPointsDiscount)));
        return new SpentPointsInfo(spentPoints, spentPointsRef);
    }

    @Override
    public AppliedPointsResponse calculateCartPointsDiscount(List<CartItem> items, Set<Long> points, boolean yeshteryCart) {
        if (points == null || points.isEmpty()) {
            return new AppliedPointsResponse(ZERO, Collections.emptyList());
        }
        List<AppliedPoints> appliedPoints = new ArrayList<>();
        Map<Long, BigDecimal> orgWithTotalPriceMap = new HashMap<>();
        BigDecimal totalPrice = ZERO;
        for (CartItem item : items) {
            BigDecimal price = item.getPrice();
            BigDecimal discount = ofNullable(item.getDiscount()).orElse(ZERO);
            BigDecimal quantity = new BigDecimal(item.getQuantity());
            BigDecimal total = (price.subtract(discount)).multiply(quantity);
            if (orgWithTotalPriceMap.containsKey(item.getOrgId())) {
                orgWithTotalPriceMap.get(item.getOrgId()).add(total);
            } else {
                orgWithTotalPriceMap.put(item.getOrgId(), total);
            }
            totalPrice = totalPrice.add(total);
        }
        UserEntity user = (UserEntity) securityService.getCurrentUser();
        if (null != user.getTier()) {
            for (Map.Entry<Long, BigDecimal> e : orgWithTotalPriceMap.entrySet()) {

                OrganizationEntity org = organizationRepository.findById(e.getKey()).get();
                LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId(user.getTier().getId())
                        .orElse(null);
                if (config == null) {
                    continue;
                }
                if (yeshteryCart) {
                    user = userRepo.findByYeshteryUserIdAndOrganizationId(user.getYeshteryUserId(), e.getKey())
                            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0014, e.getKey()));
                }
                List<LoyaltyPointTransactionEntity> earnedPoints = loyaltyPointTransRepo.getTransactionsByIdInAndUserIdAndOrgId(points, user.getId(), org.getId());
                appliedPoints.addAll(calculatePointsDiscountAndCreateSpendTransactions(
                        config,
                        e.getValue(),
                        user,
                        org,
                        earnedPoints,
                        new ArrayList<>(),
                        new ArrayList<>()));
            }

            if (yeshteryCart) {
                Long yeshteryOrgId = getYeshteryOrgId();
                LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId(user.getTier().getId())
                        .orElse(null);
                if (config != null) {
                    OrganizationEntity org = organizationRepository.findById(yeshteryOrgId).get();
                    user = userRepo.findByYeshteryUserIdAndOrganizationId(user.getYeshteryUserId(), yeshteryOrgId)
                            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0014, yeshteryOrgId));
                    List<LoyaltyPointTransactionEntity> earnedPoints = loyaltyPointTransRepo.getTransactionsByIdInAndUserIdAndOrgId(points, user.getId(), org.getId());
                    appliedPoints.addAll(calculatePointsDiscountAndCreateSpendTransactions(
                            config,
                            totalPrice,
                            user,
                            org,
                            earnedPoints,
                            new ArrayList<>(),
                            new ArrayList<>()));
                }

            }
        }
        BigDecimal totalDiscount = appliedPoints.stream().map(AppliedPoints::getDiscount).reduce(ZERO, BigDecimal::add);

        return new AppliedPointsResponse(totalDiscount, appliedPoints);
    }

    @Override
    public AppliedPointsResponse calculateCartPointsDiscount(List<CartItem> items, BigDecimal totalPointsAmount, boolean yeshteryCart) {
        if (totalPointsAmount == null || totalPointsAmount.compareTo(BigDecimal.ZERO) <= 0 ) {
            return new AppliedPointsResponse(BigDecimal.ZERO, Collections.emptyList());
        }

        List<AppliedPoints> appliedPoints = new ArrayList<>();
        Map<Long, BigDecimal> orgWithTotalPriceMap = new HashMap<>();
        BigDecimal totalPrice = BigDecimal.ZERO; // Use BigDecimal.ZERO

        for (CartItem item : items) {
            BigDecimal price = item.getPrice();
            BigDecimal discount = ofNullable(item.getDiscount()).orElse(BigDecimal.ZERO); // Use BigDecimal.ZERO instead of ZERO
            BigDecimal quantity = new BigDecimal(item.getQuantity());
            BigDecimal total = (price.subtract(discount)).multiply(quantity);

            orgWithTotalPriceMap.merge(item.getOrgId(), total, BigDecimal::add); // Use merge() to simplify logic
            totalPrice = totalPrice.add(total);
        }

        UserEntity user = (UserEntity) securityService.getCurrentUser();
        if (null != user.getTier()) {
            for (Map.Entry<Long, BigDecimal> e : orgWithTotalPriceMap.entrySet()) {
                OrganizationEntity org = organizationRepository.findById(e.getKey()).orElse(null);
                if (org == null) {
                    continue;
                }

                LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId(user.getTier().getId()).orElse(null);
                if (config == null) {
                    continue;
                }

                if (yeshteryCart) {
                    // Update user for Yeshtery cart
                    user = userRepo.findByYeshteryUserIdAndOrganizationId(user.getYeshteryUserId(), e.getKey())
                            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0014, e.getKey()));
                }

                appliedPoints.addAll(calculatePointsDiscountAndCreateSpendTransactions(
                        config,
                        totalPrice,
                        totalPointsAmount)); // Pass total points amount directly
            }

            if (yeshteryCart) {
                Long yeshteryOrgId = getYeshteryOrgId();
                LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findActiveConfigTierByTierId(user.getTier().getId()).orElse(null);
                if (config != null) {
                    OrganizationEntity org = organizationRepository.findById(yeshteryOrgId).orElse(null);
                    if (org != null) {
                        user = userRepo.findByYeshteryUserIdAndOrganizationId(user.getYeshteryUserId(), yeshteryOrgId)
                                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0014, yeshteryOrgId));
                        appliedPoints.addAll(calculatePointsDiscountAndCreateSpendTransactions(
                                config,
                                totalPrice,
                                totalPointsAmount)); // Pass total points amount directly
                    }
                }
            }
        }

        BigDecimal totalDiscount = appliedPoints.stream().map(AppliedPoints::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add); // Use BigDecimal.ZERO instead of ZERO

        return new AppliedPointsResponse(totalDiscount, appliedPoints);
    }



    private List<AppliedPoints> calculatePointsDiscountAndCreateSpendTransactions(
            LoyaltyPointConfigEntity config, BigDecimal totalWithoutShipping, BigDecimal totalPointsAmount) {
        List<AppliedPoints> res = new ArrayList<>();
        LoyaltyConfigConstraint constraint = getConfigConstraint(config, ORDER_ONLINE);
        BigDecimal to = ofNullable(constraint.getRatioTo()).orElse(BigDecimal.ZERO);
        BigDecimal from = ofNullable(constraint.getRatioFrom()).orElse(BigDecimal.ZERO);
        BigDecimal tmp = (totalPointsAmount.multiply(to)).divide(from, 2, RoundingMode.HALF_EVEN);
        res.add(new AppliedPoints(null, tmp));
        return res;
    }


    private List<AppliedPoints> calculatePointsDiscountAndCreateSpendTransactions(LoyaltyPointConfigEntity config,
                                                                                  BigDecimal totalWithoutShipping,
                                                                                  UserEntity user,
                                                                                  OrganizationEntity org,
                                                                                  List<LoyaltyPointTransactionEntity> earnedPoints,
                                                                                  List<LoyaltyPointTransactionEntity> spentPoints,
                                                                                  List<LoyaltySpentTransactionEntity> spentPointsRef) {
        List<AppliedPoints> res = new ArrayList<>();

        BigDecimal total = ZERO;
        BigDecimal pointsAmount;
        for (LoyaltyPointTransactionEntity earnedPoint : earnedPoints) {
            LoyaltyConfigConstraint constraint = getConfigConstraint(config, LoyaltyPointType.getLoyaltyPointType(earnedPoint.getType()));
            BigDecimal to = ofNullable(constraint.getRatioTo()).orElse(ZERO);
            BigDecimal from = ofNullable(constraint.getRatioFrom()).orElse(ZERO);

            BigDecimal tmp = (earnedPoint.getPoints().multiply(to)).divide(from);
            if (totalWithoutShipping.subtract(total.add(tmp)).compareTo(ZERO) >= 0) {
                total = total.add(tmp);
                pointsAmount = earnedPoint.getPoints();
            } else if (total.compareTo(totalWithoutShipping) >= 0) {
                break;
            } else {
                tmp = totalWithoutShipping.subtract(total);
                total = total.add(tmp);
                pointsAmount = tmp.multiply(from).divide(to);
            }
            LoyaltyPointTransactionEntity spendPoint = new LoyaltyPointTransactionEntity();
            spendPoint.setStartDate(earnedPoint.getStartDate());
            spendPoint.setEndDate(earnedPoint.getEndDate());
            spendPoint.setOrganization(org);
            spendPoint.setUser(user);
            spendPoint.setIsValid(true);
            spendPoint.setPoints(pointsAmount);
            spendPoint.setType(SPEND_IN_ORDER.getValue());

            LoyaltySpentTransactionEntity spentPointRef = new LoyaltySpentTransactionEntity();
            spentPointRef.setTransaction(earnedPoint);
            spentPointRef.setReverseTransaction(spendPoint);

            spentPoints.add(spendPoint);
            spentPointsRef.add(spentPointRef);

            res.add(new AppliedPoints(earnedPoint.getId(), tmp));
        }
        return res;
    }

    private LoyaltyConfigConstraint getConfigConstraint(LoyaltyPointConfigEntity entity, LoyaltyPointType type) {
        return readConfigJsonStr(entity.getConstraints()).getOrDefault(type, null);
    }

    private LoyaltyConfigConstraint getConfigConstraint(LoyaltyPointConfigEntity entity, LoyaltyTransactions type) {
        return readConfigJson(entity.getConstraints()).getOrDefault(type, null);
    }

    private HashMap<LoyaltyPointType, LoyaltyConfigConstraint> readConfigJsonStr(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<HashMap<LoyaltyPointType, LoyaltyConfigConstraint>>() {
            });
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0001, jsonStr);
        }
    }


    private HashMap<LoyaltyTransactions, LoyaltyConfigConstraint> readConfigJson(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<HashMap<LoyaltyTransactions, LoyaltyConfigConstraint>>() {
            });
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0001, jsonStr);
        }
    }

    @Override
    public List<LoyaltyPointTransactionDTO> getUserSpendablePointsForCartOrganizations() {
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if (!(baseUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity currentUser = (UserEntity) baseUser;
        List<Long> orgIds = cartItemRepo
                .findCurrentCartItemsByUser_Id(currentUser.getId())
                .stream()
                .map(CartItemEntity::getStock)
                .map(StocksEntity::getOrganizationEntity)
                .map(OrganizationEntity::getId)
                .collect(toList());
        orgIds.add(getYeshteryOrgId());
        return loyaltyPointTransRepo
                .getSpendablePointsByUserIdAndOrgIds(currentUser.getYeshteryUserId(), orgIds)
                .stream()
                .map(t -> {
                    BigDecimal spentPoints = t.getSpentTransactions()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(LoyaltySpentTransactionEntity::getReverseTransaction)
                            .map(LoyaltyPointTransactionEntity::getPoints)
                            .reduce(ZERO, BigDecimal::add);
                    t.setPoints(t.getPoints().subtract(spentPoints));
                    return t;
                }).map(LoyaltyPointTransactionEntity::getRepresentation)
                .collect(toList());
    }

    public List<LoyaltyPointTransactionDTO> getUserSpendablePointsForOrganization(Long orgId) {
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if (!(baseUser instanceof UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity currentUser = (UserEntity) baseUser;
        return loyaltyPointTransRepo.getSpendablePointsByUserIdAndOrgId(currentUser.getYeshteryUserId(), orgId).stream()
                .map(t -> {
            BigDecimal spentPoints = t.getSpentTransactions().stream().filter(Objects::nonNull).map(LoyaltySpentTransactionEntity::getReverseTransaction).map(LoyaltyPointTransactionEntity::getPoints).reduce(ZERO, BigDecimal::add);
            t.setPoints(t.getPoints().subtract(spentPoints));
            return t;
        }).map(LoyaltyPointTransactionEntity::getRepresentation).collect(toList());
    }

    @Override
    public void givePointsToReferrer(UserEntity user, Long orgId) {
        Optional<OrganizationEntity> org = organizationRepository.findById(orgId);
        if(org.isPresent()){
            prepareLoyaltyPointTransaction(user, org.get(), REFERRAL, null, false);
        }else {
            throw new RuntimeException("Organization not exist");
        }

    }

    @Override
    public void activateReferralPoints(OrdersEntity suborder) {
        MetaOrderEntity metaOrder = suborder.getMetaOrder();
        if (metaOrder.getSubMetaOrder() != null) {
            metaOrder = metaOrder.getSubMetaOrder();
            Long orgId = metaOrder.getOrganization().getId();
            Long yeshteryUserId = metaOrder.getUser().getYeshteryUserId();
            UserEntity user = userRepo.findByReferralUserIdAndOrganizationId(yeshteryUserId, orgId);
            if (user != null)
                loyaltyPointTransRepo.setTransactionAsValidByUserId(user.getId());
        }
    }

    private Long getYeshteryOrgId() {
        return ofNullable(organizationRepository.findByPname(YESHTERY_PNAME))
                .map(OrganizationEntity::getId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0002));
    }

    private Integer getAvailablePoints(ShopsEntity shop, UserEntity user) {
        if (shop.getAllowOtherPoints()) {
            return loyaltyPointTransRepo.findAllRedeemablePoints(user.getId());
        } else {
            return loyaltyPointTransRepo.findOrgRedeemablePoints(user.getId(), shop.getOrganizationEntity().getId());
        }
    }

    @Deprecated
    @Override
    @Transactional
    public void sharePoints(Long orgId, String email, BigDecimal points) {

        List<LoyaltyPointTransactionDTO> validLoyaltyPointTrans = getPointsTransByUserAndOrg(orgId);
        BigDecimal totalPoints = calculateTotalPoints(validLoyaltyPointTrans);

        if (points.compareTo(totalPoints) >= 0) {
            throw new RuntimeBusinessException(METHOD_NOT_ALLOWED, ORG$LOY$0025);
        }

        UserEntity user = userRepo.getByEmailAndOrganizationId(email, orgId);
        Optional<OrganizationEntity> org = organizationRepository.findById(orgId);

        if (!(user instanceof UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0010, email);
        }
        LoyaltyPointType type = LoyaltyPointType.getLoyaltyPointType(5);

        LoyaltyPointTransactionEntity spendPoint = createLoyaltyPointTransaction(org.get(), user, points, validLoyaltyPointTrans.get(0).getEndDate());
        spendPoint.setType(type.getValue());
        spendPoint.setIsValid(true);
        loyaltyPointTransRepo.save(spendPoint);
        deductAndCreateLoyaltySpendTransaction(validLoyaltyPointTrans,spendPoint);
    }

    @Override
    @Transactional
    public void sharePointsBetweenUsers(Long orgId, String email, BigDecimal points) {
        UserEntity senderUser = getCurrentUserWithOrganization();
        OrganizationEntity org = organizationRepository.findById(orgId).orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, G$ORG$0002));
        userLoyaltyPointsRepository.save(processTransaction(senderUser, points , TRANSFER_POINTS ,org,null,null,null));
        UserEntity receiverUser = userRepo.getByEmailAndOrganizationId(email, orgId);
        if (receiverUser == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0010, email);
        }
        userLoyaltyPointsRepository.save(  processTransaction(receiverUser, points , SHARE_POINTS ,org,null,null,null));
    }

    private List<LoyaltyPointTransactionDTO> getPointsTransByUserAndOrg(Long orgId) {
        BaseUserEntity currentUser = securityService.getCurrentUser();
        List<LoyaltyPointTransactionEntity> validPointsTrans = loyaltyPointTransRepo.findByUser_IdAndOrganization_IdAndIsValidAndStartDateBeforeAndPointsGreaterThanOrderByCreatedAtAsc
                (currentUser.getId(), orgId, true, LocalDateTime.now(), BigDecimal.valueOf(0));

        List<LoyaltyPointTransactionDTO> validPointsTransDTOs = validPointsTrans.stream().map(LoyaltyPointTransactionEntity::getRepresentation).collect(toList());
        validPointsTrans.forEach(t -> {
            BigDecimal spentPoints = t.getSpentTransactions().stream().filter(Objects::nonNull).map(LoyaltySpentTransactionEntity::getReverseTransaction).map(LoyaltyPointTransactionEntity::getPoints).reduce(ZERO, BigDecimal::add);
            validPointsTransDTOs.forEach(loyaltyPointTransactionDTO -> {
                if (loyaltyPointTransactionDTO.getId() == t.getId()){
                    loyaltyPointTransactionDTO.setPoints(t.getPoints().subtract(spentPoints));
                }
            });

        });
        return validPointsTransDTOs;
    }

    private BigDecimal calculateTotalPoints(List<LoyaltyPointTransactionDTO> validPointsTrans) {
        BigDecimal totalPoints = new BigDecimal(0);
        for (int i = 0; i < validPointsTrans.size(); i++) {
            totalPoints = totalPoints.add(new BigDecimal(String.valueOf(validPointsTrans.get(i).getPoints())));
        }
        return totalPoints;
    }

    @Transactional
    private void deductAndCreateLoyaltySpendTransaction(List<LoyaltyPointTransactionDTO> validPointsTrans, LoyaltyPointTransactionEntity spendPoint) {
        BigDecimal deductedPoints = spendPoint.getPoints();
        for (LoyaltyPointTransactionDTO validPointsTransaction : validPointsTrans) {
            if (deductedPoints.compareTo(validPointsTransaction.getPoints()) > 0) {
                deductedPoints = new BigDecimal(String.valueOf(deductedPoints.subtract(validPointsTransaction.getPoints())));
//                validPointsTransaction.setPoints(new BigDecimal(0));
//                validPointsTransaction.setIsValid(false);
//                validPointsTransaction.setEndDate(LocalDateTime.now());
//                loyaltyPointTransRepo.save(validPointsTransaction);
                LoyaltySpentTransactionEntity spentPointTrans = new LoyaltySpentTransactionEntity();
                spentPointTrans.setTransaction(loyaltyPointTransRepo.findById(validPointsTransaction.getId()).get());
                spentPointTrans.setReverseTransaction(spendPoint);
                loyaltySpendTransactionRepo.save(spentPointTrans);
            } else {
                BigDecimal diff = new BigDecimal(String.valueOf(validPointsTransaction.getPoints().subtract(deductedPoints)));
//                validPointsTransaction.setPoints(diff);
//                loyaltyPointTransRepo.save(validPointsTransaction);
                LoyaltySpentTransactionEntity spentPointTrans = new LoyaltySpentTransactionEntity();
                spentPointTrans.setTransaction(loyaltyPointTransRepo.findById(validPointsTransaction.getId()).get());
                spentPointTrans.setReverseTransaction(spendPoint);
                loyaltySpendTransactionRepo.save(spentPointTrans);
                break;
            }
        }
    }

    private LoyaltyPointTransactionEntity createLoyaltyPointTransaction(OrganizationEntity org,
                                                                        UserEntity user,
                                                                        BigDecimal points,
                                                                        LocalDateTime expiry) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setIsValid(false);
        entity.setUser(user);
        entity.setOrganization(org);
        entity.setStartDate(calculateTransactionStartDate(org));
        entity.setEndDate(expiry);
        return entity;
    }

    @Override
    public List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return listOrganizationLoyaltyPoints(orgId);
    }

    @Override
    public LoyaltyTierDTO getUserOrgTier() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return getUserOrgTier(orgId);
    }

    @Override
    public List<LoyaltyPointTransactionDTO> getUserSpendablePointsForAuthUserOrganization() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return getUserSpendablePointsForOrganization(orgId);
    }


    @Override
   public UserLoyaltyPoints processTransaction(UserEntity userId, BigDecimal points, LoyaltyTransactions type , OrganizationEntity org , ShopsEntity shop , OrdersEntity order, MetaOrderEntity yeshteryMetaOrder ) {
       UserLoyaltyPoints userPoints = getUserPoints(userId, type);
       userPoints.addTransactions(buildTransaction(points, org, shop, order, yeshteryMetaOrder, type));
       switch (type) {
            case ORDER_ONLINE:
            case SHARE_POINTS:
            case REFERRAL:
                userPoints.depositPoints(points);
                break;
            case TRANSFER_POINTS:
            case REDEEM_POINTS:
            case SPEND_IN_ORDER:
            case PICKUP_FROM_SHOP:
                validateAndProcessWithdraw(points,userPoints);
                break;
        }
        return userPoints;

    }

    private UserLoyaltyPoints  getUserPoints(UserEntity userId ,LoyaltyTransactions type  ) {
      return   userLoyaltyPointsRepository.findByUser(userId)
                .orElseGet(() -> {
                    if (type == LoyaltyTransactions.ORDER_ONLINE ||
                            type == LoyaltyTransactions.SHARE_POINTS ||
                            type == LoyaltyTransactions.REFERRAL) {
                        return buildBasicEntity(userId, BigDecimal.ZERO);
                    } else {
                        throw new RuntimeBusinessException(NOT_FOUND,U$0001);
                    }
                });
    }

    private void validateAndProcessWithdraw(BigDecimal amount, UserLoyaltyPoints userPoints){
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeBusinessException(BAD_REQUEST,LOY$PARAM$0006);
        }
        if (amount.compareTo(userPoints.getBalance()) > 0) {
            throw new RuntimeBusinessException(METHOD_NOT_ALLOWED, ORG$LOY$0025);
        }
        userPoints.withdrawBalance(amount);
    }

    private UserLoyaltyTransactions buildTransaction(BigDecimal points , OrganizationEntity org , ShopsEntity shop , OrdersEntity order, MetaOrderEntity yeshteryMetaOrder, LoyaltyTransactions type ){
    UserLoyaltyTransactions entity = new UserLoyaltyTransactions();
        entity.setAmount(points);
        entity.setOrganization(org);
        entity.setShop(shop);
        entity.setOrder(order);
        entity.setMetaOrder(yeshteryMetaOrder);
        entity.setType(type.name());
        entity.setDescription(type.getDescription());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
