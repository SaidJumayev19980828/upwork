package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.OrganizationPoints;
import com.nasnav.response.LoyaltyPointDeleteResponse;
import com.nasnav.response.LoyaltyPointsUpdateResponse;
import com.nasnav.response.LoyaltyUserPointsResponse;
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

import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.enumerations.LoyaltyPointType.*;
import static com.nasnav.enumerations.OrderStatus.DELIVERED;
import static com.nasnav.enumerations.Settings.RETURN_DAYS_LIMIT;
import static com.nasnav.enumerations.ShippingStatus.PICKED_UP;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.OrderReturnServiceImpl.MAX_RETURN_TIME_WINDOW;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class LoyaltyPointsServiceImpl implements LoyaltyPointsService{
    private static final Logger logger = LogManager.getLogger("LoyaltyPointsService");
    @Autowired
    private SecurityService securityService;
    @Autowired
    private LoyaltyTierService loyaltyTierService;
    @Autowired
    private LoyaltyPointTypeRepository loyaltyPointTypeRepo;

    @Autowired
    private LoyaltyPointRepository loyaltyPointRepo;

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

            if(dto.getDefaultTier() != null && dto.getDefaultTier().getId() != null) {
                setConfigDefaultTier(dto.getDefaultTier().getId(), org.getId(), entity);
            }
        } else {
            loyaltyPointConfigRepo.setAllOrgConfigsAsInactive(org.getId());

            entity.setIsActive(true);
            entity.setOrganization(org);
            setConfigDefaultTier(dto.getDefaultTier().getId(), org.getId(), entity);
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if ( dto.getConstraints() != null && !dto.getConstraints().isEmpty()) {
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
            logger.error(e,e);
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

    @Override
    public void createLoyaltyPointCharityTransaction(LoyaltyCharityEntity charity, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isDonate) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setIsValid(true);
        entity.setUser(user);
        entity.setCharity(charity);
        entity.setShop(shopEntity);
        loyaltyPointTransRepo.save(entity);
    }

    @Override
    public LoyaltyPointsUpdateResponse createLoyaltyPointGiftTransaction(LoyaltyGiftEntity gift, UserEntity user, BigDecimal points, Boolean isGift) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setIsValid(true);
        entity.setUser(user);
        entity.setGift(gift);
        loyaltyPointTransRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

    @Override
    public LoyaltyPointsUpdateResponse createLoyaltyPointCoinsDropTransaction(LoyaltyCoinsDropEntity coins, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isCoinsDrop) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setIsValid(true);
        entity.setUser(user);
        entity.setCoinsDrop(coins);
        entity.setShop(shopEntity);
        loyaltyPointTransRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

    @Override
    public LoyaltyUserPointsResponse getUserPoints(Long orgId) {
        UserEntity user = getCurrentUserWithOrg(orgId);

        Integer points = loyaltyPointTransRepo.findOrgRedeemablePoints(user.getId(), orgId);
        return new LoyaltyUserPointsResponse(points);
    }

    @Override
    public List<OrganizationPoints> getUserPointsPerOrg() {
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if(! (baseUser instanceof  UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity currentUser = (UserEntity) baseUser;
        return loyaltyPointTransRepo.findRedeemablePointsPerOrg(currentUser.getYeshteryUserId());
    }

    private UserEntity getCurrentUserWithOrg(Long orgId) {
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if(! (baseUser instanceof  UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity currentUser = (UserEntity) baseUser;

        return getUserEntity(orgId, currentUser.getYeshteryUserId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0014, orgId));
    }

    private Optional<UserEntity> getUserEntity(Long orgId, Long yeshteryId) {
        return userRepo.findByYeshteryUserIdAndOrganizationId(yeshteryId, orgId);
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
                    dto.setConstraints(loyaltyTierService.readTierJsonStr(entity.getConstraints()));
                    return dto;
                })                .
                orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0021, userEntity.getId()));
    }

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
        entity.setType(ORDER_ONLINE.getValue());
        loyaltyPointTransRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

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

        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(org.getId(), TRUE).orElse(null);
        if (config == null) {
            return;
        }
        Optional<UserEntity> userEntityOp = userRepo.findById(order.getUserId());
        if(userEntityOp.isEmpty()) {
            return;
        }
        UserEntity userEntity = userEntityOp.get();
        if(userEntity.getTier() == null) {
            return;
        }
        BigDecimal points = calculatePoints(config, userEntity.getTier(), pointsAmount, ORDER_ONLINE);
        createLoyaltyPointTransaction(shop, org, userEntity, null, order, points, pointsAmount, getConfigConstraint(config, type).getExpiry());
    }

    private BigDecimal getTierCoefficientByType(LoyaltyTierEntity entity, LoyaltyPointType type) {
        return loyaltyTierService.readTierJsonStr(entity.getConstraints()).get(type);
    }

    @Override
    public void createYeshteryLoyaltyPointTransaction(MetaOrderEntity yeshteryMetaOrder, LoyaltyPointType type, BigDecimal pointsAmount) {
        OrganizationEntity org = yeshteryMetaOrder.getOrganization();
        UserEntity user = yeshteryMetaOrder.getUser();

        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(org.getId(), TRUE).orElse(null);
        if (config == null) {
            return;
        }
        BigDecimal points = calculatePoints(config, user.getTier(), pointsAmount, ORDER_ONLINE);
        createLoyaltyPointTransaction(null, org, user, yeshteryMetaOrder, null, points, pointsAmount, getConfigConstraint(config, type).getExpiry());
    }

    private BigDecimal calculatePoints(LoyaltyPointConfigEntity config, LoyaltyTierEntity tier, BigDecimal amount, LoyaltyPointType type) {
        BigDecimal coefficient = getTierCoefficientByType(tier, type);

        LoyaltyConfigConstraint constraint = getConfigConstraint(config, type);
        BigDecimal from = ofNullable(constraint.getRatioFrom()).orElse(ZERO);
        BigDecimal to = ofNullable(constraint.getRatioTo()).orElse(ZERO);
        BigDecimal localAmount = ofNullable(amount).orElse(constraint.getAmount());

        if(anyIsNull(from, to , coefficient, localAmount)) {
            logger.warn(ORG$LOY$0002.getValue());
            return BigDecimal.ZERO;
        }
        return localAmount.multiply(coefficient).multiply(from).divide(to, 2, RoundingMode.HALF_EVEN);
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

        prepareLoyaltyPointTransaction(user, org, PICKUP_FROM_SHOP, order.getTotal());
        loyaltyPinsRepository.delete(pinEntity);
    }

    private void prepareLoyaltyPointTransaction(UserEntity user, OrganizationEntity org, LoyaltyPointType type, BigDecimal amount) {
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(org.getId(), TRUE)
                .orElse(null);
        if (config == null || user.getTier() == null) {
            return;
        }
        BigDecimal points = calculatePoints(config, user.getTier(), amount, type);
        LoyaltyConfigConstraint constraint = getConfigConstraint(config, type);
        LoyaltyPointTransactionEntity transaction = createLoyaltyPointTransaction(org,
                user,
                points,
                constraint.getAmount(),
                constraint.getExpiry());
        transaction.setType(type.getValue());
        transaction.setIsValid(true);
        loyaltyPointTransRepo.save(transaction);
    }


    @Override
    public List<LoyaltyPointTransactionDTO> listOrganizationLoyaltyPoints(Long orgId) {
        UserEntity user = getCurrentUserWithOrg(orgId);
        return loyaltyPointTransRepo.findByUser_IdAndOrganization_Id(user.getId(), orgId)
             .stream()
             .map(LoyaltyPointTransactionEntity::getRepresentation)
             .collect(toList());
    }

    @Override
    public List<LoyaltyPointTypeDTO> listLoyaltyPointTypes() {
        return loyaltyPointTypeRepo.findAll()
                .stream()
                .map(LoyaltyPointTypeEntity::getRepresentation)
                .collect(toList());
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
    public SpentPointsInfo applyPointsOnOrders(Set<Long> points,
                                               Set<OrdersEntity> subOrders,
                                               BigDecimal totalWithoutShipping,
                                               Long userId,
                                               OrganizationEntity org) {
        UserEntity user = userRepo.findById(userId).get();
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(org.getId(), true)
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
        List<AppliedPoints> appliedPoints = new ArrayList<>();
        Map<Long, BigDecimal> orgWithTotalPriceMap = new HashMap<>();
        BigDecimal totalPrice = ZERO;
        for(CartItem item : items) {
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
        for (Map.Entry<Long, BigDecimal> e : orgWithTotalPriceMap.entrySet()) {

            OrganizationEntity org = organizationRepository.findById(e.getKey()).get();
            LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(org.getId(), true)
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
            LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(yeshteryOrgId, true)
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
        BigDecimal totalDiscount = appliedPoints.stream().map(AppliedPoints::getDiscount).reduce(ZERO, BigDecimal::add);

        return new AppliedPointsResponse(totalDiscount, appliedPoints);
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
            }
            else if (total.compareTo(totalWithoutShipping) >= 0) {
                break;
            }
            else {
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

    private HashMap<LoyaltyPointType, LoyaltyConfigConstraint> readConfigJsonStr(String jsonStr){
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<HashMap<LoyaltyPointType, LoyaltyConfigConstraint>>() {});
        } catch (Exception e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0001, jsonStr);
        }
    }

    @Override
    public List<LoyaltyPointTransactionDTO> getUserSpendablePoints() {
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if(! (baseUser instanceof  UserEntity)) {
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
                })
                .map(LoyaltyPointTransactionEntity::getRepresentation)
                .collect(toList());
    }

    @Override
    public void givePointsToReferrer(UserEntity user, Long orgId) {
        OrganizationEntity org = organizationRepository.findById(orgId).get();
        prepareLoyaltyPointTransaction(user, org, REFERRAL, null);
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
}
