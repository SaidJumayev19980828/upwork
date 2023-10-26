package com.nasnav.test;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.service.SecurityService;
import com.nasnav.service.impl.subscription.StripeSubscriptionServiceImpl;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Stripe_Subscription_Service_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class StripeSubscriptionServiceTest extends AbstractTestWithTempBaseDir {

    @MockBean
    private SecurityService securityService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private StripeSubscriptionServiceImpl stripeSubscriptionServiceImpl;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Test
    public void checkCustomerNotExistSuccess() {

        Mockito.when(securityService.getCurrentUserOrganization()).thenReturn(organizationRepository.findById(99001l).get());
        boolean hasStripeCustomer = stripeSubscriptionServiceImpl.checkOrgHasStripeCustomer();
        assertTrue(!hasStripeCustomer);

    }

    @Test
    public void checkCustomerExistSuccess() {

        Mockito.when(securityService.getCurrentUserOrganization()).thenReturn(organizationRepository.findById(99002l).get());
        boolean hasStripeCustomer = stripeSubscriptionServiceImpl.checkOrgHasStripeCustomer();
        assertTrue(hasStripeCustomer);

    }

    @Test
    public void createCustomerAlreadyExistsSuccess() {
        Mockito.when(securityService.getCurrentUserOrganization()).thenReturn(organizationRepository.findById(99002l).get());
        String customerId = stripeSubscriptionServiceImpl.getOrCreateStripeCustomer();
        assertTrue(customerId.equals("12346454654645"));

    }

    @Test
    @Transactional
    public void createStripeCustomerSuccess() {
        Mockito.when(securityService.getCurrentUserOrganization()).thenReturn(organizationRepository.findById(99001l).get());
        String customerId = stripeSubscriptionServiceImpl.getOrCreateStripeCustomer();
        assertTrue(!StringUtils.isBlankOrNull(customerId));
    }








}
