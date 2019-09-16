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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.CsvHeaderNamesDTO;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.response.ProductListImportResponse;

@Service
public class DataImportService {

	
	@Autowired
	private ShopsRepository shopRepo;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	
	
	public ProductListImportResponse importProductListFromCSV(@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException {
		
		validateProductImportMetaData(importMetaData);
		validateProductImportCsvFile(file);
		
		return new ProductListImportResponse(Collections.emptyList());
	}
	
	
	
	

	private void validateProductImportCsvFile(@Valid MultipartFile file) throws BusinessException{
		if(file == null || file.isEmpty()) {
			throw new BusinessException(
					ERR_NO_FILE_UPLOADED
					, "INVALID PARAM"
					, HttpStatus.NOT_ACCEPTABLE); 
		}
		
	}





	private void validateProductImportMetaData(@Valid ProductListImportDTO metaData) throws BusinessException{
		Long shopId = metaData.getShopId();
		String encoding = metaData.getEncoding();
		Integer currency = metaData.getCurrency();
		CsvHeaderNamesDTO headerNames = metaData.getHeaders();
		
		if( anyIsNull(shopId, encoding, currency, headerNames)) {
			throw new BusinessException(
					ERR_PRODUCT_IMPORT_MISSING_PARAM
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}		
		
		validateShopId(shopId);		
		
		validateEncodingCharset(encoding);		
		
		validateStockCurrency(currency);
		
		validateCsvHeaderNames( headerNames);
	}





	private void validateCsvHeaderNames(CsvHeaderNamesDTO headers) throws BusinessException{
		if(anyIsNull(headers.getBarcode(), headers.getName(), headers.getCategory(), headers.getPrice(), headers.getQuantity())) {
			throw new BusinessException(
					ERR_PRODUCT_IMPORT_MISSING_HEADER_NAME 
					, "MISSING PARAM:csv-header(s)"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
	}





	private void validateEncodingCharset(String encoding) throws BusinessException {
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
	}





	private void validateShopId(Long shopId) throws BusinessException {
		if( !shopRepo.existsById( shopId ) ) {
			throw new BusinessException(
					String.format(ERR_SHOP_ID_NOT_EXIST, shopId)
					, "MISSING PARAM:shop_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		EmployeeUserEntity user =  empRepo.getOneByEmail(auth.getName());
		Long userOrgId = user.getOrganizationId();
		
		ShopsEntity shop = shopRepo.findById(shopId).get();
		Long shopOrgId = shop.getOrganizationEntity().getId();
		
		if(!Objects.equals(shopOrgId, userOrgId)) {
			throw new BusinessException(
					String.format(ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP, userOrgId, shopOrgId)
					, "MISSING PARAM:shop_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
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
