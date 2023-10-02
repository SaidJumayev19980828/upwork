package com.nasnav.test;

import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.jobs.AdvertisementJob;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;


@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Advertisements_Job_Init_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class AdvertisementJobInitTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private AdvertisementJob advertisementJob;

    @Autowired
    private BankAccountActivityService bankAccountActivityService;

    @Test
    void log() {
        Float oldSenderBalance = bankAccountActivityService.getAvailableBalance(10L);
        Float oldReceiverBalance = bankAccountActivityService.getAvailableBalance(11L);
        advertisementJob.calculateLikes();
        Float newSenderBalance = bankAccountActivityService.getAvailableBalance(10L);
        Float newReceiverBalance = bankAccountActivityService.getAvailableBalance(11L);
        MatcherAssert.assertThat(oldSenderBalance, CoreMatchers.is(Matchers.greaterThan(newSenderBalance)));
        MatcherAssert.assertThat(newReceiverBalance, CoreMatchers.is(Matchers.greaterThan(oldReceiverBalance)));
    }

}
