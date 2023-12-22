package com.nasnav.service.impl.subscription;

import com.nasnav.dao.PackageRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Component("wert")
public class WertSubscriptionServiceImpl extends SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    BankInsideTransactionService bankInsideTransactionService;
    @Autowired
    PackageService packageService;
    @Autowired
    PackageRepository packageRepository;

    @Autowired
    CurrencyPriceBlockChainService currencyPriceBlockChainService;
    @Autowired
    private SecurityService securityService;


    @Override
    public SubscriptionDTO getPaymentInfo(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {

        //Get Package Registered In Org
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        Long packageId = packageService.getPackageIdRegisteredInOrg(org);
        if(packageId == null){
            throw new RuntimeBusinessException(NOT_FOUND, ORG$SUB$0001);
        }

        //Get Amount in Coins
        PackageEntity packageEntity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PA$USR$0002, packageId));
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
