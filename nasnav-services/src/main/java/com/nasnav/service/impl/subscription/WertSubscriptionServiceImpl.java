package com.nasnav.service.impl.subscription;

import com.nasnav.dao.SubscriptionRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.service.BankInsideTransactionService;
import com.nasnav.service.CurrencyPriceBlockChainService;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Component("wert")
public class WertSubscriptionServiceImpl extends SubscriptionServiceImpl implements SubscriptionService {


    private final BankInsideTransactionService bankInsideTransactionService;
    private final PackageService packageService;
    private final CurrencyPriceBlockChainService currencyPriceBlockChainService;
    private final SecurityService securityService;

    @Autowired
    public WertSubscriptionServiceImpl(BankInsideTransactionService bankInsideTransactionService,
                                       PackageService packageService, CurrencyPriceBlockChainService currencyPriceBlockChainService,
                                       SecurityService securityService, SubscriptionRepository subscriptionRepository) {
        super(securityService, packageService, subscriptionRepository );
        this.bankInsideTransactionService = bankInsideTransactionService;
        this.packageService = packageService;
        this.currencyPriceBlockChainService = currencyPriceBlockChainService;
        this.securityService = securityService;
    }


    @Override
    public SubscriptionDTO getPaymentInfo(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {

        //Get Package Registered In Org
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        PackageEntity packageEntity = packageService.getPackageRegisteredInOrg(org);
        if (packageEntity == null){
            throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001);
        }
        if(packageEntity.getCountry() == null || packageEntity.getCountry().getCurrency() == null){
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, ORG$SUB$0002);
        }
        String currency = packageEntity.getCountry().getCurrency();
        Float currencyPrice = currencyPriceBlockChainService.getCurrencyPrice(currency);
        BigDecimal amount = packageEntity.getPrice().divide(new BigDecimal(currencyPrice),0, RoundingMode.HALF_UP);
        subscriptionDTO.setCurrency("coins");
        subscriptionDTO.setPaidAmount(amount);
        return subscriptionDTO;
    }

    @Override
    @Transactional
    public SubscriptionDTO subscribe(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {
        subscriptionDTO = super.subscribe(subscriptionDTO);
        subscriptionDTO = getPaymentInfo(subscriptionDTO);
        //Bank Account Pay with Wallet
        bankInsideTransactionService.pay(subscriptionDTO.getPaidAmount().floatValue());

        //Save Subscription
        subscriptionDTO.setType(SubscriptionMethod.WERT.getValue());
        return savePackageSuccessfulSubscription(subscriptionDTO);
    }
}
