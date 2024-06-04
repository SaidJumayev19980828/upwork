package com.nasnav.test;


import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.ReferralCodeRepo;
import com.nasnav.dao.ReferralSettingsRepo;
import com.nasnav.dao.ReferralWalletRepository;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ReferralCodeEntity;
import com.nasnav.persistence.ReferralTransactions;
import com.nasnav.service.InfluencerReferralrWalletServiceImpl;
import com.nasnav.service.ReferralCodeService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.impl.InfluencerReferralServiceImpl;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@NotThreadSafe
public class ReferralServiceUnitTest extends AbstractTestWithTempBaseDir {

    @MockBean
    private ReferralWalletRepository referralWalletRepository;

    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    private InfluencerReferralServiceImpl influencerReferralService;

    @Autowired
    private InfluencerReferralrWalletServiceImpl influencerReferralrWalletService;

    @MockBean
    private ReferralSettingsRepo referralSettingsRepo;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private ReferralCodeService referralCodeService;

    @MockBean
    private ReferralCodeRepo referralCodeRepo;

    @Test
    public void influencerNotFoundWhenGettingWallet() {
        when(referralWalletRepository.findByUserIdAndReferralType(any(Long.class), any(ReferralType.class)))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> influencerReferralrWalletService.getWalletByUserId(1L))
                .isInstanceOf(RuntimeBusinessException.class)
                .hasMessageContaining("No Referral Wallet Found for that User 1.");

    }

    @Test
    public void testValidateWithdrawThrowException() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal balance = new BigDecimal("50.00");

        assertThatThrownBy(() -> influencerReferralrWalletService.validateWithdraw(amount, balance))
                .isInstanceOf(RuntimeBusinessException.class)
                .hasMessageContaining("Insufficient Fund for that Customer");
    }

    @Test
    public void exceptionWhileReadingJson() throws IOException {
        when(objectMapper.readValue(any(String.class) , any(Class.class)))
                .thenThrow(new RuntimeException("Simulated JsonProcessingException"));

        assertThatThrownBy(() -> influencerReferralService.readConfigJsonStr(
                        "{\"start_Date\":\"2024-04-29\",\"endDate\":\"2024-05-29\",\"products\":[1,2,3],\"cashbackValue\": 20.0}"))
                .isInstanceOf(RuntimeBusinessException.class)
                .hasMessageContaining("Failed to read json into an object!");
    }

    @Test
    public void getEmptyMessageStringForNonSupportedActivity() {
        assertEquals("", referralCodeService.getActivityMessageByType(null,
                ReferralTransactionsType.ORDER_DISCOUNT));
    }

    @Test
    public void getExceptionWhenThereIsNoReferralWithCode() {
        String code = "abcdef";
        when(referralCodeRepo.findByReferralCodeAndReferralType(anyString(), any(ReferralType.class)))
                .thenThrow(new RuntimeException("There is no existing referral code with code " + code));

        ReferralTransactions referralTransactions = new ReferralTransactions();
        ReferralCodeEntity referralCodeEntity = new ReferralCodeEntity();
        referralCodeEntity.setReferralCode(code);
        referralTransactions.setReferralCodeEntity(referralCodeEntity);

        assertThatThrownBy(() -> referralCodeService.getActivityMessageByType(referralTransactions,
                ReferralTransactionsType.ORDER_SHARE_REVENUE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("There is no existing referral code with code " + code);

    }

//    @Test
//    public void exceptionWhileWritingJson() throws JsonProcessingException {
//        when(objectMapper.writeValueAsString(any(Object.class)))
//                .thenThrow(new RuntimeException("Simulated JsonProcessingException"));
//
//        assertThatThrownBy(() -> influencerReferralService.writeConfigJsonStr(
//                InfluencerReferralConstraints
//                        .builder()
//                        .startDate(LocalDate.now())
//                        .build()))
//                .isInstanceOf(RuntimeBusinessException.class)
//                .hasMessageContaining("Failed to write json from an Object!");
//    }


    @Test
    public void checkIntervalReturnFalseWhenReferralSettingsIsNull() {
        when(referralSettingsRepo.findByReferralTypeAndOrganizationId(any(ReferralType.class), any(Long.class)))
                .thenReturn(Optional.empty());

        when(securityService.getCurrentUserOrganizationId()).thenReturn(1L);

        assertFalse(referralCodeService.checkIntervalDateForCurrentOrganization(
                ReferralCodeType.PAY_WITH_REFERRAL_WALLET));

    }

    @Test
    public void WithdrawReturnFalseWhenReferralSettingsIsNull() {
        when(referralSettingsRepo.findByReferralTypeAndOrganizationId(any(ReferralType.class), any(Long.class)))
                .thenReturn(Optional.empty());

        when(securityService.getCurrentUserOrganizationId()).thenReturn(1L);

        assertFalse(referralCodeService.withDrawFromReferralWallet(
                null));

    }

    @Test
    public void testGenerateReferralCode_WithConflict() {
        when(referralCodeRepo.existsByAcceptReferralToken(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String referralCode = referralCodeService.generateReferralCodeToken();

        assertNotNull(referralCode);
        assertEquals(8, referralCode.length());
        verify(referralCodeRepo, times(2)).existsByAcceptReferralToken(anyString());
    }

}
