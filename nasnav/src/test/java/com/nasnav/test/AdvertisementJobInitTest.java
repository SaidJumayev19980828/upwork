package com.nasnav.test;

import com.nasnav.service.jobs.AdvertisementJob;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;


@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Advertisements_Job_Init_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class AdvertisementJobInitTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private AdvertisementJob advertisementJob;


    @Test
    void log() {
        advertisementJob.calculateLikes();
    }

}
