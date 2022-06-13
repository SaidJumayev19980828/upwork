package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.SpentPointsInfo;
import com.nasnav.dto.request.*;
import com.nasnav.dto.response.LoyaltyPointTransactionDTO;
import com.nasnav.dto.response.LoyaltyPointsCartResponseDto;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.OrganizationPoints;
import com.nasnav.response.LoyaltyPointDeleteResponse;
import com.nasnav.response.LoyaltyPointsUpdateResponse;
import com.nasnav.response.LoyaltyUserPointsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.enumerations.Settings.RETURN_DAYS_LIMIT;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.OrderReturnServiceImpl.MAX_RETURN_TIME_WINDOW;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class LoyaltyPointsServiceImpl implements LoyaltyPointsService{
    private static final Logger logger = LogManager.getLogger("LoyaltyPointsService");
    @Autowired
    private SecurityService securityService;

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
    private ShopsRepository shopsRepo;
    @Autowired
    private CartItemRepository cartItemRepo;
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private SettingRepository settingRepo;
    @Autowired
    private LoyaltyPinsRepository loyaltyPinsRepository;

    @Autowired
    private LoyaltyTierRepository loyaltyTierRepository;

    @Override
    public LoyaltyPointsUpdateResponse updateLoyaltyPointType(LoyaltyPointTypeDTO dto) {
        if (isBlankOrNull(dto.getName())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0001);
        }
        LoyaltyPointTypeEntity entity = ofNullable(dto.getId())
                .map(loyaltyPointTypeRepo::findById)
                .map(Optional::get)
                .orElseGet(LoyaltyPointTypeEntity::new);
        entity.setName(dto.getName());
        loyaltyPointTypeRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

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
        if(dto.getRatioFrom() != null){
            entity.setRatioFrom(dto.getRatioFrom());
        }
        if(dto.getRatioTo() != null){
            entity.setRatioTo(dto.getRatioTo());
        }
        if(dto.getCoefficient() != null){
            entity.setCoefficient(dto.getCoefficient());
        }
        if(dto.getExpiry() != null) {
            entity.setExpiry(dto.getExpiry());
        }

        return loyaltyPointConfigRepo.save(entity);
    }

    private void setConfigDefaultTier(Long tierId, Long orgId, LoyaltyPointConfigEntity entity) {
        LoyaltyTierEntity tier = loyaltyTierRepository.findByIdAndOrganization_Id(tierId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0019, tierId));
        entity.setDefaultTier(tier);
    }

    private void validateLoyaltyPointConfigDTO(LoyaltyPointConfigDTO dto) {
        if (dto.getId() == null) {
            if (anyIsNull(dto, dto.getCoefficient(), dto.getRatioFrom(), dto.getRatioTo(), dto.getDescription(),
                    dto.getDefaultTier(), dto.getDefaultTier().getId()))
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0008);
        }
    }

    @Override
    public LoyaltyPointsUpdateResponse updateLoyaltyPoint(LoyaltyPointDTO dto) {
        validateLoyaltyPointDTO(dto);
        LoyaltyPointEntity entity = prepareLoyaltyPointEntity(dto);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

    @Override
    public LoyaltyPointDeleteResponse deleteLoyaltyPointType(Long id) {
        LoyaltyPointTypeEntity entity = loyaltyPointTypeRepo.findById(id)
                 .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0004, id));
        if (loyaltyPointRepo.countByType_Id(id) > 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0005);
        }
        loyaltyPointTypeRepo.delete(entity);
        return new LoyaltyPointDeleteResponse(true, entity.getId());
    }

    @Override
    public LoyaltyPointDeleteResponse deleteLoyaltyPoint(Long id) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        LoyaltyPointEntity entity = loyaltyPointRepo.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0006, id));
        if (loyaltyPointTransRepo.countByLoyaltyPoint_Id(id) > 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0007);
        }
        loyaltyPointRepo.delete(entity);
        return new LoyaltyPointDeleteResponse(true, entity.getId());
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
    @Transactional
    public LoyaltyPointsUpdateResponse terminateLoyaltyPoint(Long id) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        LoyaltyPointEntity entity = loyaltyPointRepo.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0006, id));
        entity.setEndDate(LocalDateTime.now());
        loyaltyPointRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

    @Override
    public void createLoyaltyPointCharityTransaction(LoyaltyCharityEntity charity, UserEntity user, BigDecimal points, ShopsEntity shopEntity, Boolean isDonate) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setIsValid(true);
        entity.setUser(user);
        entity.setCharity(charity);
        entity.setIsDonate(isDonate);
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
        entity.setIsGift(isGift);
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
        entity.setIsCoinsDrop(isCoinsDrop);
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

        Optional<ShopsEntity> shop = shopsRepo.findById(shopId);
        if(shop.isEmpty()) {
            throw new RuntimeBusinessException(NOT_FOUND, S$0002, shopId);
        }
        BaseUserEntity user = securityService.getCurrentUser();

        if(!(user instanceof UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001, user.getId());
        }

        SecureRandom random = new SecureRandom();
        int num = random.nextInt(100000);
        String formattedPin = format("%05d", num);

        LoyaltyPinsEntity pinsEntity = new LoyaltyPinsEntity();
        pinsEntity.setShop(shop.get());
        pinsEntity.setUser((UserEntity) user);
        pinsEntity.setPin(formattedPin);
        loyaltyPinsRepository.save(pinsEntity);
        return formattedPin;
    }

    @Override
    public List<LoyaltyPointsCartResponseDto> getUserPointsGroupedByOrg(Long yeshteryUserId, List<CartItem> items) {
        List<LoyaltyPointsCartResponseDto> result = new ArrayList<>();

        Set<Long> orgIds = items.stream().collect(Collectors.groupingBy(CartItem::getOrgId)).keySet();
        for (Long orgId : orgIds) {
            Optional<UserEntity> user = getUserEntity(orgId, yeshteryUserId);
            if(user.isEmpty()) {
                continue;
            }
            Integer points = loyaltyPointTransRepo.findOrgRedeemablePointsByOrgAndYeshteryUserId( yeshteryUserId, orgId);
            LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(orgId, TRUE).orElse(null);
            if (config == null) {
                continue;
            }
            LoyaltyTierEntity tier = user.get().getTier();

            if(anyIsNull(points, tier, tier.getCoefficient())) {
                continue;
            }
            BigDecimal amounts = calculateAmounts(config, points, tier.getCoefficient());
            result.add(new LoyaltyPointsCartResponseDto(orgId, points, amounts));
        }
        return result;
    }

    private LoyaltyTierDTO getLoyaltyTierDTO(UserEntity userEntity) {
        return ofNullable(userEntity.getTier())
                .map(LoyaltyTierEntity::getRepresentation)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0021, userEntity.getId()));
    }

    @Override
    public LoyaltyPointsUpdateResponse createLoyaltyPointTransaction(ShopsEntity shop, OrganizationEntity org,
                                                                     UserEntity user,
                                                                     MetaOrderEntity yeshteryMetaOrder,
                                                                     OrdersEntity order, BigDecimal points,
                                                                     BigDecimal amount, Integer expiry) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setAmount(amount);
        entity.setShop(shop);
        entity.setIsValid(true);
        entity.setUser(user);
        entity.setOrder(order);
        entity.setMetaOrder(yeshteryMetaOrder);
        entity.setOrganization(org);
        entity.setStartDate(calculateTransactionStartDate(org));
        entity.setEndDate(calculateTransactionEndDate(entity.getStartDate(), expiry));
        loyaltyPointTransRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
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
    public void createLoyaltyPointTransaction(OrdersEntity order, BigDecimal pointsAmount) {
        OrganizationEntity org = order.getOrganizationEntity();
        ShopsEntity shop = order.getShopsEntity();
        UserEntity user = order.getMetaOrder().getUser();

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
        LoyaltyTierDTO tier = userEntity.getTier().getRepresentation();
        BigDecimal points = calculatePoints(config, pointsAmount, tier.getCoefficient());
        createLoyaltyPointTransaction(shop, org, userEntity, null, order, points, pointsAmount, config.getExpiry());
    }

    @Override
    public void createYeshteryLoyaltyPointTransaction(MetaOrderEntity yeshteryMetaOrder, BigDecimal pointsAmount) {
        OrganizationEntity org = yeshteryMetaOrder.getOrganization();
        UserEntity user = yeshteryMetaOrder.getUser();

        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(org.getId(), TRUE).orElse(null);
        if (config == null) {
            return;
        }
        LoyaltyTierDTO tier = user.getTier().getRepresentation();
        BigDecimal points = calculatePoints(config, pointsAmount, tier.getCoefficient());
        createLoyaltyPointTransaction(null, org, user, yeshteryMetaOrder, null, points, pointsAmount, config.getExpiry());
    }

    private BigDecimal calculatePoints(LoyaltyPointConfigEntity config, BigDecimal amount, BigDecimal coefficient) {
        BigDecimal from = config.getRatioFrom();
        BigDecimal to = config.getRatioTo();

        if(anyIsNull(from, to , coefficient, amount)) {
            logger.warn(ORG$LOY$0002.getValue());
            return BigDecimal.ZERO;
        }
        return amount.multiply(coefficient).multiply(from).divide(to, 2, RoundingMode.HALF_EVEN);
    }


    private BigDecimal calculateAmounts(LoyaltyPointConfigEntity config, Integer points, BigDecimal coefficient) {
        BigDecimal from = config.getRatioFrom();
        BigDecimal to = config.getRatioTo();

        if(anyIsNull(from, to , coefficient, points)) {
            logger.warn( format( ORG$LOY$0006.getValue(), config.getOrganization().getId()));
            return BigDecimal.ZERO;
        }
        return new BigDecimal(points).multiply(coefficient).multiply(to).divide(from, 2, RoundingMode.HALF_EVEN);
    }

    @Override
    public void createLoyaltyPointTransactionForReturnRequest(ReturnRequestEntity returnRequest) {
        Set<OrdersEntity> orders = returnRequest
                .getReturnedItems()
                .stream()
                .map(ReturnRequestItemEntity::getBasket)
                .map(BasketsEntity::getOrdersEntity)
                .collect(toSet());

        createReturnTransactionsForOrders(orders);
    }

    @Override
    public LoyaltyPointsUpdateResponse redeemPoints(Long pointId, Long userId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        ShopsEntity shop = securityService.getCurrentUserShop();
        LoyaltyPointEntity entity = loyaltyPointRepo.findByIdAndOrganization_Id(pointId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0006, pointId));
        UserEntity user = userRepo.findByIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));

        Integer availablePoints = getAvailablePoints(shop, user);
        List<RedeemPointsOfferDTO> offers = getRedeemOffers(orgId, availablePoints);
        if (offers.stream().noneMatch(p -> p.getPointId().equals(pointId))) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0012, pointId);
        }

        LoyaltyPointTransactionEntity transaction = new LoyaltyPointTransactionEntity();
        transaction.setPoints(entity.getAmount());
        transaction.setShop(shop);
        transaction.setIsValid(true);
        transaction.setUser(user);
        transaction.setLoyaltyPoint(entity);
        loyaltyPointTransRepo.save(transaction);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

    private void createReturnTransactionsForOrders(Set<OrdersEntity> orders) {
        Set<Long> orderIds = orders
                .stream()
                .map(OrdersEntity::getId)
                .collect(toSet());
        Set<Long> metaOrderIds = orders
                .stream()
                .map(OrdersEntity::getMetaOrder)
                .map(MetaOrderEntity::getSubMetaOrder)
                .filter(Objects::nonNull)
                .map(MetaOrderEntity::getId)
                .collect(toSet());
        List<LoyaltyPointTransactionEntity> ordersTransactions = loyaltyPointTransRepo.findByOrderIdInOrYeshteryMetaOrderIdIn(orderIds, metaOrderIds);
        List<LoyaltyPointTransactionEntity> returnTransactions = new ArrayList<>();

        for (LoyaltyPointTransactionEntity transaction : ordersTransactions) {
            LoyaltyPointTransactionEntity returnTransaction = new LoyaltyPointTransactionEntity();
            BeanUtils.copyProperties(transaction, returnTransaction);
            returnTransaction.setAmount(transaction.getAmount().negate());
            returnTransaction.setPoints(transaction.getPoints().negate());
            returnTransactions.add(returnTransaction);
        }
        loyaltyPointTransRepo.saveAll(returnTransactions);
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
                .map(LoyaltyPointConfigEntity::getRepresentation)
                .collect(toList());
    }

    @Override
    public LoyaltyPointConfigDTO getLoyaltyPointActiveConfig() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(orgId, true)
                .map(LoyaltyPointConfigEntity::getRepresentation)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0024, orgId));
    }

    @Override
    public List<RedeemPointsOfferDTO> checkRedeemPoints(String code) {
        UserEntity user = (UserEntity)securityService.getCurrentUser();
        ShopsEntity shop = shopsRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0007, code));
        Integer availablePoints = getAvailablePoints(shop, user);
        return getRedeemOffers(user.getOrganizationId(), availablePoints);
    }

    @Override
    public SpentPointsInfo applyPointsOnOrders(Set<Long> points, Set<OrdersEntity> subOrders, Long userId, OrganizationEntity org) {
        UserEntity user = userRepo.findById(userId).get();
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(org.getId(), true)
                .orElse(null);
        if (config == null) {
            return new SpentPointsInfo();
        }
        BigDecimal to = config.getRatioTo();
        BigDecimal from = config.getRatioFrom();
        BigDecimal total = ZERO;
        BigDecimal pointsAmount = ZERO;
        BigDecimal totalWithoutShipping =
                subOrders
                        .stream()
                        .map(o -> o.getSubTotal().subtract(o.getDiscounts()))
                        .reduce(ZERO, BigDecimal::add);

        List<LoyaltyPointTransactionEntity> earnedPoints = loyaltyPointTransRepo.getTransactionsByIdInAndUserIdAndOrgId(points, userId, org.getId());
        List<LoyaltyPointTransactionEntity> spentPoints = new ArrayList<>();
        List<LoyaltySpentTransactionEntity> spentPointsRef = new ArrayList<>();
        for (LoyaltyPointTransactionEntity earnedPoint : earnedPoints) {
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

            LoyaltySpentTransactionEntity spentPointRef = new LoyaltySpentTransactionEntity();
            spentPointRef.setTransaction(earnedPoint);
            spentPointRef.setReverseTransaction(spendPoint);

            spentPoints.add(spendPoint);
            spentPointsRef.add(spentPointRef);
        }
        BigDecimal suborderPointsDiscount = total.divide(new BigDecimal(subOrders.size()));
        subOrders.forEach(s -> s.setDiscounts(s.getDiscounts().add(suborderPointsDiscount)));
        return new SpentPointsInfo(spentPoints, spentPointsRef);
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

    private Integer getAvailablePoints(ShopsEntity shop, UserEntity user) {
        if (shop.getAllowOtherPoints()) {
            return loyaltyPointTransRepo.findAllRedeemablePoints(user.getId());
        } else {
            return loyaltyPointTransRepo.findOrgRedeemablePoints(user.getId(), shop.getOrganizationEntity().getId());
        }
    }

    private List<RedeemPointsOfferDTO> getRedeemOffers(Long orgId, Integer availablePoints) {
        return loyaltyPointRepo
                .findByOrganization_IdAndAmountLessThanEqual(orgId, availablePoints)
                .stream()
                .map(this::toRedeemPointsOfferDTO)
                .collect(toList());
    }

    private RedeemPointsOfferDTO toRedeemPointsOfferDTO(LoyaltyPointEntity loyaltyPointEntity) {
        RedeemPointsOfferDTO dto = new RedeemPointsOfferDTO();
        dto.setPointId(loyaltyPointEntity.getId());
        dto.setPoints(loyaltyPointEntity.getPoints());
        return dto;
    }


    private LoyaltyPointEntity prepareLoyaltyPointEntity(LoyaltyPointDTO dto) {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        Long orgId = org.getId();
        LoyaltyPointEntity entity = new LoyaltyPointEntity();
        if (dto.getId() != null) {
            entity = loyaltyPointRepo.findByIdAndOrganization_Id(dto.getId(), orgId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0006, dto.getId()));
        } else {
            entity.setOrganization(org);
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getTypeId() != null) {
            LoyaltyPointTypeEntity type = loyaltyPointTypeRepo.findById(dto.getTypeId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0003));
            entity.setType(type);
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }
        if (dto.getAmount() != null) {
            entity.setAmount(dto.getAmount());
        }
        if (dto.getPoints() != null) {
            entity.setPoints(dto.getPoints());
        }
        return loyaltyPointRepo.save(entity);
    }

    private void validateLoyaltyPointDTO(LoyaltyPointDTO dto) {
        if (dto.getId() == null) {
            if (anyIsNull(dto, dto.getTypeId(), dto.getStartDate(), dto.getEndDate(), dto.getAmount(), dto.getPoints())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0002);
            }
            if (dto.getStartDate().isAfter(dto.getEndDate())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0009);
            }
        }
    }
}
