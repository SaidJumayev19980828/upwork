package com.nasnav.service;

import static com.nasnav.constatnts.error.dataimport.ErrorMessages.*;
import static com.nasnav.persistence.EntityUtils.anyIsNull;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductListImportResponse;

@Service
public class DataImportService {

	
	@Autowired
	private ShopsRepository shopRepo;
	
	
	
	public ProductListImportResponse importProductListFromCSV(@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException {
		
		validateProductImportMetaData(importMetaData);
		
		return new ProductListImportResponse(Collections.emptyList());
	}
	
	
	
	

	private void validateProductImportMetaData(@Valid ProductListImportDTO metaData) throws BusinessException{
		Long shopId = metaData.getShopId();
		String encoding = metaData.getEncoding();
		Integer currency = metaData.getCurrency();
		
		
		if( anyIsNull(shopId, encoding, currency, metaData.getHeaders())) {
			throw new BusinessException(
					ERR_MISSING_PRODUCT_IMPORT_PARAM
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		
		if( !shopRepo.existsById( shopId ) ) {
			throw new BusinessException(
					String.format(ERR_SHOP_ID_NOT_EXIST, shopId)
					, "MISSING PARAM:shop_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		
		try {
			if( !Charset.isSupported(encoding)) {
				throw new IllegalStateException();
			}			
		}catch(Exception e) {
			throw new BusinessException(
					String.format(ERR_INVALID_ENCODING, encoding)
					, "MISSING PARAM:encoding"
					, HttpStatus.NOT_ACCEPTABLE);
		}		
		
		
		validateStockCurrency(currency);
		
	}
	
	
	
	
	
	private void validateStockCurrency(Integer currency) throws BusinessException {
		boolean invalidCurrency = Arrays.asList( TransactionCurrency.values() )
										.stream()
										.map(TransactionCurrency::getValue)
										.map(Integer::valueOf)
										.noneMatch(val -> Objects.equals(currency, val));
		if(invalidCurrency ) {
			throw new BusinessException(
					String.format("Invalid Currency code [%d]!", currency)
					, "INVALID_PARAM:currency" 
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}

}
