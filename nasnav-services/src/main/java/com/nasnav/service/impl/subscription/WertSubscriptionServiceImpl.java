package com.nasnav.service.impl.subscription;

import com.nasnav.dao.PackageRepository;
import com.nasnav.dto.SubscriptionDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.enumerations.SubscriptionMethod;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.service.BankInsideTransactionService;
import com.nasnav.service.CurrencyPriceBlockChainService;
import com.nasnav.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.nasnav.exceptions.ErrorCodes.ORG$SUB$0002;
import static com.nasnav.exceptions.ErrorCodes.PA$USR$0002;
import static org.springframework.http.HttpStatus.*;

@Service
public class WertSubscriptionServiceImpl extends SubscriptionServiceImpl{

    @Autowired
    BankInsideTransactionService bankInsideTransactionService;
    @Autowired
    PackageService packageService;
    @Autowired
    PackageRepository packageRepository;

    @Autowired
    CurrencyPriceBlockChainService currencyPriceBlockChainService;


    @Override
    public SubscriptionDTO subscribe(SubscriptionDTO subscriptionDTO) throws RuntimeBusinessException {

        //Register Package In Profile
        PackageRegisteredByUserDTO packageRegisteredByUserDTO = new PackageRegisteredByUserDTO();
        packageRegisteredByUserDTO.setPackageId(subscriptionDTO.getPackageId());
        packageService.registerPackageProfile(packageRegisteredByUserDTO);

        //Get Amount in Coins
        PackageEntity packageEntity = packageRepository.findById(subscriptionDTO.getPackageId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_ACCEPTABLE, PA$USR$0002, subscriptionDTO.getPackageId()));
        if(packageEntity.getCountry() == null || packageEntity.getCountry().getCurrency() == null){
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, ORG$SUB$0002);
        }
        String currency = packageEntity.getCountry().getCurrency();
        Float currencyPrice = currencyPriceBlockChainService.getCurrencyPrice(currency);
        BigDecimal amount = packageEntity.getPrice().divide(new BigDecimal(currencyPrice),0, RoundingMode.HALF_UP);

        //Bank Account Pay with Wallet
//        bankInsideTransactionService.pay(amount.floatValue());

        //Save Subscription
        subscriptionDTO.setType(SubscriptionMethod.WERT.getValue());
        subscriptionDTO.setPaidAmount(amount);
        return savePackageSuccessfulSubscription(subscriptionDTO);
    }


}
