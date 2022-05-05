package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.request.LoyaltyPointConfigDTO;
import com.nasnav.dto.request.LoyaltyPointDTO;
import com.nasnav.dto.request.LoyaltyPointTypeDTO;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.dto.response.LoyaltyPointsCartResponseDto;
import com.nasnav.dto.response.RedeemPointsOfferDTO;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
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
import java.util.stream.Collectors;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
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
    private LoyaltyPointConfigRepository loyaltyPointConfigRepo;

    @Autowired
    private ShopsRepository shopsRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrganizationRepository organizationRepository;

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
        loyaltyPointConfigRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
    }

    private LoyaltyPointConfigEntity prepareLoyaltyPointConfigEntity(LoyaltyPointConfigDTO dto) {
        Long orgId = dto.getOrgId();
        OrganizationEntity org = getOrganizationEntity(orgId);

        LoyaltyPointConfigEntity entity = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(orgId, TRUE).orElse(new LoyaltyPointConfigEntity());
        LoyaltyPointConfigEntity oldEntity = entity;
        // don't delete a config just make it inactive and create new one
        if(entity != null && entity.getId() != null && entity.getId() > 0 ) {
            oldEntity.setIsActive(false);
            entity = new LoyaltyPointConfigEntity(entity);
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
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        } else {
            entity.setIsActive(true);
        }
        if(dto.getDefaultTier() != null && dto.getDefaultTier().getId() != null) {
            Optional<LoyaltyTierEntity> tier = loyaltyTierRepository.findByIdAndOrganization_Id(dto.getDefaultTier().getId(), orgId);
            if(tier.isEmpty()) {
                throw new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0019, dto.getDefaultTier().getId());
            }
             entity.setDefaultTier(tier.get());
        }
        if(oldEntity.getId() != null && oldEntity.getId() > 0) {
            loyaltyPointConfigRepo.save(oldEntity);
        }
        entity.setOrganization(org);
        return entity;
    }

    private OrganizationEntity getOrganizationEntity(Long orgId) {
        Optional<OrganizationEntity> org = organizationRepository.findById(orgId);
        if(org.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001);
        }
        return org.get();
    }

    private void validateLoyaltyPointConfigDTO(LoyaltyPointConfigDTO dto) {
        Long orgId = dto.getOrgId();
        if (dto.getId() == null) {
            if (anyIsNull(dto, dto.getOrgId())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0008);
            }
        } else {
            if (!loyaltyPointConfigRepo.existsByIdAndOrganization_IdAndIsActive(dto.getId(), orgId, TRUE)) {
                throw new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0018, dto.getId(), orgId);
            }
        }
    }

    @Override
    public LoyaltyPointsUpdateResponse updateLoyaltyPoint(LoyaltyPointDTO dto) {
        validateLoyaltyPointDTO(dto);
        LoyaltyPointEntity entity = prepareLoyaltyPointEntity(dto);
        loyaltyPointRepo.save(entity);
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
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if(! (baseUser instanceof  UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity currentUser = (UserEntity)baseUser;
        Optional<UserEntity> user = getUserEntity(orgId, currentUser.getYeshteryUserId());

        if(user.isEmpty()){
            return new LoyaltyUserPointsResponse(false, currentUser.getId(), 0);
        }

        Integer points = loyaltyPointTransRepo.findOrgRedeemablePoints(user.get().getId(), orgId);
        return new LoyaltyUserPointsResponse(true, currentUser.getId(), points);
    }

    private Optional<UserEntity> getUserEntity(Long orgId, Long yeshteryId) {
        return userRepo.findByYeshteryUserIdAndOrganizationId(yeshteryId, orgId);
    }

    @Override
    public LoyaltyTierDTO getUserOrgTier(Long orgId) {
        BaseUserEntity baseUser = securityService.getCurrentUser();

        if(! (baseUser instanceof  UserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
        }
        UserEntity currentUser = (UserEntity)baseUser;
        Optional<UserEntity> user = getUserEntity(orgId, currentUser.getYeshteryUserId());

        if(user.isEmpty()){
            throw new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0006, orgId);
        }
        UserEntity userEntity = user.get();

        return getLoyaltyTierDTO(orgId, userEntity);
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
            LoyaltyTierDTO tier = getLoyaltyTierDTO(orgId, user.get());

            if(anyIsNull(points, tier, tier.getCoefficient())) {
                continue;
            }
            BigDecimal amounts = calculateAmounts(config, points, tier.getCoefficient());
            result.add(new LoyaltyPointsCartResponseDto(orgId, points, amounts));
        }
        return result;
    }

    @Override
    public LoyaltyPointConfigDTO updateOrgDefaultTier(Long orgId, Long tierId) {
        LoyaltyTierDTO tierDTO = new LoyaltyTierDTO();
        tierDTO.setId(tierId);
        tierDTO.setOrgId(orgId);

        LoyaltyPointConfigDTO dto  = new LoyaltyPointConfigDTO();
        dto.setOrgId(orgId);
        dto.setDefaultTier(tierDTO);
        LoyaltyPointConfigEntity entity = prepareLoyaltyPointConfigEntity(dto);
        loyaltyPointConfigRepo.save(entity);
        return entity.getRepresentation();
    }

    private LoyaltyTierDTO getLoyaltyTierDTO(Long orgId, UserEntity userEntity) {
        if(userEntity.getTier() != null){
            return userEntity.getTier().getRepresentation();
        }
        LoyaltyPointConfigEntity config = loyaltyPointConfigRepo.findByOrganization_IdAndIsActive(orgId, TRUE).orElseThrow( ()-> new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0006, orgId));
        userEntity.setTier(config.getDefaultTier());
        userRepo.save(userEntity);
        return userEntity.getTier().getRepresentation();
    }

    @Override
    public LoyaltyPointsUpdateResponse createLoyaltyPointTransaction(ShopsEntity shop, UserEntity user, OrdersEntity order, BigDecimal points, BigDecimal amount) {
        LoyaltyPointTransactionEntity entity = new LoyaltyPointTransactionEntity();
        entity.setPoints(points);
        entity.setShop(shop);
        entity.setIsValid(true);
        entity.setUser(user);
        entity.setOrder(order);
        entity.setOrganization(shop.getOrganizationEntity());
        entity.setAmount(amount);
        loyaltyPointTransRepo.save(entity);
        return new LoyaltyPointsUpdateResponse(entity.getId());
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
        createLoyaltyPointTransaction(shop, user, order, points, pointsAmount);
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
        UserEntity user = returnRequest.getMetaOrder().getUser();
        Set<BasketsEntity> baskets = returnRequest
                .getReturnedItems()
                .stream()
                .map(ReturnRequestItemEntity::getBasket)
                .collect(toSet());
        OrganizationEntity org = returnRequest.getMetaOrder().getOrganization();
        Set<OrdersEntity> orders = baskets
                .stream()
                .map(BasketsEntity::getOrdersEntity)
                .collect(toSet());

        setTransactionsInvalidForReturnedOrders(orders, user);

        for(OrdersEntity order : orders) {
            ShopsEntity shop = order.getShopsEntity();
            BigDecimal amount = baskets
                    .stream()
                    .filter(b -> Objects.equals(b.getOrdersEntity(), order))
                    .map(basket -> (basket.getPrice().subtract(basket.getDiscount())).multiply(basket.getQuantity()))
                    .reduce(ZERO, BigDecimal::add)
                    .subtract(order.getAmount());
            LoyaltyPointConfigEntity config = loyaltyPointConfigRepo
                    .findByOrganization_IdAndIsActive(org.getId(), TRUE).orElse(null);
            if (config == null) {
                return;
            }
            LoyaltyTierDTO tier = this.getLoyaltyTierDTO(org.getId(), user);
            BigDecimal points = calculatePoints(config, amount, tier.getCoefficient());
            createLoyaltyPointTransaction(shop, user, order, points.negate(), amount.negate());
        }
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
        if (!offers.stream().anyMatch(p -> p.getPointId() == pointId)) {
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

    private void setTransactionsInvalidForReturnedOrders(Set<OrdersEntity> orders, UserEntity user) {
        Set<Long> orderIds = orders
                .stream()
                .map(OrdersEntity::getId)
                .collect(toSet());
        loyaltyPointTransRepo.setTransactionsNotValid(user.getId(), orderIds);
    }

    @Override
    public List<LoyaltyPointTransactionEntity> listOrganizationLoyaltyPoints( Long orgId ) {
         return loyaltyPointTransRepo.findByOrganization_Id(orgId);
    }

    @Override
    public List<LoyaltyPointTypeDTO> listLoyaltyPointTypes() {
        return loyaltyPointTypeRepo.findAll()
                .stream()
                .map(LoyaltyPointTypeEntity::getRepresentation)
                .collect(toList());
    }

    @Override
    public List<LoyaltyPointConfigDTO> listLoyaltyPointConfigs(Long orgId) {
        return loyaltyPointConfigRepo.findByOrganization_IdOrderByCreatedAtDesc(orgId)
                .stream()
                .filter(LoyaltyPointConfigEntity::getIsActive)
                .map(LoyaltyPointConfigEntity::getRepresentation)
                .collect(toList());
    }

    @Override
    public List<RedeemPointsOfferDTO> checkRedeemPoints(String code) {
        UserEntity user = (UserEntity)securityService.getCurrentUser();
        ShopsEntity shop = shopsRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0007, code));
        Integer availablePoints = getAvailablePoints(shop, user);
        return getRedeemOffers(user.getOrganizationId(), availablePoints);
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
        LoyaltyPointEntity entity = loyaltyPointRepo.findByIdAndOrganization_Id(dto.getId(), orgId)
                .orElseGet(LoyaltyPointEntity::new);

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
        entity.setOrganization(org);
        return entity;
    }

    private void validateLoyaltyPointDTO(LoyaltyPointDTO dto) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (dto.getId() == null) {
            if (anyIsNull(dto, dto.getTypeId(),
                    dto.getStartDate(), dto.getEndDate(), dto.getAmount(), dto.getPoints())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0002);
            }
            if (dto.getStartDate().isAfter(dto.getEndDate())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0009);
            }
        } else {
            if (!loyaltyPointRepo.existsByIdAndOrganization_Id(dto.getId(), orgId)) {
                throw new RuntimeBusinessException(NOT_FOUND, ORG$LOY$0006, dto.getId());
            }
        }
    }
}
