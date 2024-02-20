package com.nasnav.test;


import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.ReferralTransactionRepository;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.ReferralTransactions;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.service.ReferralCodeService;
import com.nasnav.service.ReferralWalletService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.concurrent.NotThreadSafe;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
public class ReferralCodeTest  extends AbstractTestWithTempBaseDir {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ReferralCodeService referralCodeService;

    @Autowired
    private ReferralWalletService referralWalletService;

    @Autowired
    private ReferralTransactionRepository referralTransactionRepository;

    @Test
    @Transactional
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void shareRevenueForOrder(){
        OrdersEntity ordersEntity = ordersRepository.findById(330033L).get();

        referralCodeService.shareRevenueForOrder(ordersEntity);

        ReferralWallet referralWallet = referralWalletService.getWalletByUserId(89L);
        assertEquals(new BigDecimal("28.00"), referralWallet.getBalance());

        List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByReferralWallet_Id(500L);
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.ORDER_SHARE_REVENUE, referralTransactions.get(0).getType());
        assertEquals(new BigDecimal("8.00"), referralTransactions.get(0).getAmount());
    }

    @Test
    @Transactional
    @Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Referral_Code_Test_Data.sql"})
    @Sql(executionPhase=AFTER_TEST_METHOD, scripts={"/sql/database_cleanup.sql"})
    public void saveOrderDiscountTransaction(){
        Long orderId = 330033L;
        OrdersEntity ordersEntity = ordersRepository.findById(orderId).get();

        referralCodeService.saveReferralTransactionForOrderDiscount(ordersEntity);

        List<ReferralTransactions> referralTransactions = referralTransactionRepository.findByOrderId(orderId);
        assertEquals(1, referralTransactions.size());
        assertEquals(ReferralTransactionsType.ORDER_DISCOUNT, referralTransactions.get(0).getType());
        assertEquals(null, referralTransactions.get(0).getReferralWallet());
        assertEquals(new BigDecimal("6.00"), referralTransactions.get(0).getAmount());
    }



}
