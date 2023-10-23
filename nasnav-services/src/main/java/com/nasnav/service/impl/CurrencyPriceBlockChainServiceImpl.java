package com.nasnav.service.impl;

import com.nasnav.dto.request.CurrencyPriceItemDTO;
import com.nasnav.dto.request.CurrencyPriceResponseDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.CurrencyPriceBlockChainService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CurrencyPriceBlockChainServiceImpl implements CurrencyPriceBlockChainService {
    private final String BLOCK_CHAIN_BASE_URL = "https://meetusvr-blockchain-api.herokuapp.com/api/tokens/price";

    private Logger logger = LogManager.getLogger();

    @Autowired
    RestTemplate restTemplate;



    @Override
    public float getCurrencyPrice(String currency) {
        float currencyPrice;
        List<CurrencyPriceItemDTO> currencyPriceItemDTOS = fetchDataFromBlockChain();
        Optional<CurrencyPriceItemDTO> currencyPriceItem = currencyPriceItemDTOS.stream().filter(currencyPriceItemDTO -> {
            return currencyPriceItemDTO.getCurrency().equals(currency);
        }).findFirst();
        if(!currencyPriceItem.isPresent()){
            logger.error(String.format("No Price Found For [%s] in Blockchain Response",currency));
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, BC$PRI$0001);
        }
        try{
            currencyPrice = Float.parseFloat(currencyPriceItem.get().getPrice());
        }catch (Exception ex){
            logger.error(String.format("Fail To Parse Currency [%s] price [%s]",currency,currencyPriceItem.get().getPrice()));
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, BC$PRI$0001);
        }
        return currencyPrice;
    }


    private List<CurrencyPriceItemDTO> fetchDataFromBlockChain() {

        ResponseEntity<CurrencyPriceResponseDTO> response =
                restTemplate.getForEntity(BLOCK_CHAIN_BASE_URL, CurrencyPriceResponseDTO.class);
        if(!response.getBody().getStatus()){
            logger.error(String.format("Fail To Fetch Currencies Price From Blockchain"));
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, BC$PRI$0001);
        }
        return response.getBody().getData();
    }
}
