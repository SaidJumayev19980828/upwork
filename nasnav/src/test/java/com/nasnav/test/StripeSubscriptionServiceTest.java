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

@TestPropertySource(properties = "stripe.apikey = sk_test_51NxqlfGR4qGEOW4E6Qni6REIWcwheVdU8mf2LtTVn1BWn8dtdQSg7stf9b0cqE8CJZVja9aTuOISKg15qC52CjLf00bmLe17sU")
@TestPropertySource(properties = "stripe.webhook.secret= whsec_c6c1772b65026654a21e1beac00f0a213eacbb56edd1bca45a8fdfd10fdb1c6c")
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
