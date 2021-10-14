package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.request.CoinsDropDTO;
import com.nasnav.enumerations.LoyaltyEvents;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.CoinsDropEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.CoinUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
@EnableAsync
public class CoinsDropServiceImp implements CoinsDropService {

    @Autowired
    CoinsDropRepository coinsDropRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserService userService;
    @Autowired
    CoinsDropLogsService coinsDropLogsService;
    @Autowired
    LoyaltyPointsService loyaltyPointsService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LoyaltyPointTransactionRepository loyaltyTransactionRepository;
    @Autowired
    ShopsRepository shopsRepository;

    @Override
    public CoinUpdateResponse updateCoinsDrop(CoinsDropDTO coins) {
        validateCoinsDrop(coins);

        CoinsDropEntity entity = createCoinsDropEntity(coins);
        coinsDropRepository.save(entity);
        return new CoinUpdateResponse(entity.getId());
    }

    @Override
    public List<CoinsDropEntity> getByOrganizationId(Long orgId) {
        return coinsDropRepository.getByOrganization_Id(orgId);
    }

    @Override
    public CoinsDropEntity getByOrganizationIdAndTypeId(Long orgId, Long typeId) {
        return coinsDropRepository.getByOrganization_IdAndTypeId(orgId, typeId);
    }

    // run this method every day 86400000 in milliseconds means 24 hours
    @Scheduled(fixedRate = 86400000)
    @Override
    public void giveUsersCoinsBirthDay() {
        List<UserEntity> users = loadUsersAllowReward();
        for (UserEntity user : users) {
            if (LocalDate.now().equals(user.getDateOfBirth().toLocalDate())) {
                Integer typeId = LoyaltyEvents.BIRTH_DAY.getValue();
                giveUserCoinsByTypeId(user, typeId.longValue());
            }
        }
    }

    @Scheduled(fixedRate = 86400000)
    @Override
    public void giveUsersCoinsOfficialFestival() {
        List<UserEntity> users = loadUsersAllowReward();
        Long orgId = securityService.getCurrentUserOrganizationId();
        for (UserEntity user : users) {
            List<CoinsDropEntity> coins = coinsDropRepository.findByOfficialVacationDateNotNullAndOrganization_Id(orgId);
            for (CoinsDropEntity coin : coins) {
                if (coin.getOfficialVacationDate() != null
                        && LocalDate.now().equals(coin.getOfficialVacationDate())) {
                    Integer typeId = LoyaltyEvents.GLOBAL_DATE_FESTIVAL.getValue();
                    giveUserCoinsByTypeId(user, typeId.longValue());
                }
            }
        }
    }

    @Scheduled(fixedRate = 86400000)
    @Override
    public void giveUsersCoinsOfficialRamadan() {
        List<UserEntity> users = loadUsersAllowReward();
        for (UserEntity user : users) {
            if (isRamadanPointTransaction(LocalDateTime.now().toLocalDate())) {
                Integer typeId = LoyaltyEvents.GLOBAL_DATE_RAMADAN.getValue();
                giveUserCoinsByTypeId(user, typeId.longValue());
            }
        }
    }

    @Override
    public void giveUserCoinsNewTier(UserEntity user) {
        Integer typeId = LoyaltyEvents.NEW_TIER.getValue();
        giveUserCoinsByTypeId(user, typeId.longValue());
    }

    @Override
    public void giveUserCoinsInvitationUsers(UserEntity user) {
        Integer typeId = LoyaltyEvents.CUSTOMER_INVITE.getValue();
        giveUserCoinsByTypeId(user, typeId.longValue());
    }

    @Override
    public void giveUserCoinsSignUp(UserEntity user) {
        Integer typeId = LoyaltyEvents.SIGN_UP.getValue();
        giveUserCoinsByTypeId(user, typeId.longValue());
    }

    @Override
    public void giveUserCoinsNewFamilyMember(UserEntity user) {
        Integer typeId = LoyaltyEvents.NEW_FAMILY_MEMBER.getValue();
        giveUserCoinsByTypeId(user, typeId.longValue());
    }

    @Override
    public void giveUserCoinsNewFamilyPurchase(UserEntity user) {
        Integer typeId = LoyaltyEvents.NEW_FAMILY_PURCHASE.getValue();
        giveUserCoinsByTypeId(user, typeId.longValue());
    }

    private void giveUserCoinsByTypeId(UserEntity userEntity, Long typeId) {
        List<OrganizationEntity> organizations = organizationRepository.findAllOrganizations();
        for (OrganizationEntity org : organizations) {
            List<ShopsEntity> shops = shopsRepository.findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(org.getId(), 0);
            shops.forEach(shop -> updateUserCoinsByTypeIdAndShopId(org.getId(), userEntity, typeId, shop.getId()));
        }
    }

    private void updateUserCoinsByTypeIdAndShopId(Long orgId, UserEntity userEntity, Long typeId, Long shopId){
        CoinsDropEntity entity = coinsDropRepository.getByOrganization_IdAndTypeId(orgId, typeId);
        //Create Coins Drop Log
        coinsDropLogsService.updateCoinsDropLog(entity);
        Integer amount = entity.getAmount();

        ShopsEntity shopEntity = shopsRepository.findById(shopId).get();

        // Create Transactions
        loyaltyPointsService.updateLoyaltyPointCoinsDropTransaction(entity, userEntity, amount, shopEntity, true);
        
    }
    private List<UserEntity> loadUsersAllowReward() {
        return userService.getYeshteryUsersByAllowReward(true);
    }

    private CoinsDropEntity createCoinsDropEntity(CoinsDropDTO coins) {
        CoinsDropEntity entity = getOrCreateCoinsDropEntity(coins);
        if (isUpdateOperation(coins)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , COINS$PARAM$0002, coins.getId());
        }
        OrganizationEntity organization = securityService.getCurrentUserOrganization();
        Integer type = LoyaltyEvents.getLoyaltyEvents(coins.getTypeId()).getValue();

        entity.setOrganization(organization);
        entity.setTypeId(type);
        entity.setAmount(coins.getAmount());
        entity.setIsActive(coins.getIsActive());
        entity.setOfficialVacationDate(coins.getOfficialVacationDate());
        return entity;
    }

    private CoinsDropEntity getOrCreateCoinsDropEntity(CoinsDropDTO coins) {
        return ofNullable(coins)
                .map(CoinsDropDTO::getId)
                .map(this::getExistingCoinsDrop)
                .orElseGet(CoinsDropEntity::new);
    }

    private CoinsDropEntity getExistingCoinsDrop(Long id) {
        return ofNullable(id)
                .flatMap(coinsDropRepository::findById)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
                        , COINS$PARAM$0001, id));
    }

    private boolean isUpdateOperation(CoinsDropDTO coins) {
        return nonNull(coins.getId());
    }

    private void validateCoinsDrop(CoinsDropDTO coins) {
        if (anyIsNull(coins, coins.getTypeId(), coins.getAmount())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , COINS$PARAM$0003, coins.toString());
        }
    }

    private Boolean isRamadanPointTransaction(LocalDate currentDate) {
        HijrahDate ramadan = HijrahDate.now()
                .with(ChronoField.DAY_OF_MONTH, 1).with(ChronoField.MONTH_OF_YEAR, 9);
        LocalDate startRamadan = LocalDate.from(ramadan);
        LocalDate endRamadan = LocalDate.from(ramadan.with(TemporalAdjusters.lastDayOfMonth()));
        if (currentDate.isAfter(startRamadan) || currentDate.isBefore(endRamadan)) {
            Long userId = securityService.getCurrentUser().getId();
            Integer countTransaction = loyaltyTransactionRepository.getCoinsDropTransactionsByUser_IdAndCreatedAt(userId, startRamadan, endRamadan);
             return (countTransaction <= 0);
        }
        return false;
    }

}
