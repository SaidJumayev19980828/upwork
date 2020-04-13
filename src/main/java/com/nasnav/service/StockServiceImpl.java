package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_MISSING_STOCK_UPDATE_PARAMS;
import static com.nasnav.enumerations.Roles.ORGANIZATION_MANAGER;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.StockUpdateResponse;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.VariantBasicData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private BundleRepository bundleRepo;
    
    @Autowired
    private ShopsRepository shopRepo;

    @Autowired
    private SecurityService security;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private CachingHelper cachingHelper;

    public List<StocksEntity> getProductStockForShop(Long productId, Long shopId) throws BusinessException {
        Optional<ProductEntity> prodOpt = productRepo.findById(productId);
        
        if(prodOpt == null || !prodOpt.isPresent())
            throw new BusinessException(
            		String.format("No product exists with id [%d]!",productId)
            		, "INVALID PARAM:product_id"
            		, NOT_ACCEPTABLE);

        List<StocksEntity> stocks  = stockRepo.findByProductIdAndShopsId(productId, shopId);;

        if(stocks == null || stocks.isEmpty())
        	throw new BusinessException(
            		String.format("Product with id [%d] has no stocks!",productId)
            		, "INVALID PARAM:product_id"
            		, NOT_ACCEPTABLE);

        stocks.stream().forEach(this::updateStockQuantity);

        return stocks;
    }



    private void updateStockQuantity(StocksEntity stock) {
        if(stock == null)
            return;
        stock.setQuantity(getStockQuantity(stock));
    }


    /**
     * if the product is bundle , its quantity is limited by the lowest quantity of its items.
     * if the bundle stock quantity is set to zero , then the bundle is not active anymore.
     * Set all stocks of the bundle to the calculated quantity.
     * */
    @Transactional
    public Integer getStockQuantity(StocksEntity stock){
        ProductEntity product = Optional.ofNullable(stock.getProductVariantsEntity())
        								.map(ProductVariantsEntity::getProductEntity)
        								.orElse(null);
        if(product == null){
            return stock.getQuantity();
        }

        Integer productType = product.getProductType();

        if( productType.equals(ProductTypes.BUNDLE) ){
        	if(stock.getQuantity().equals(0))
        		return 0;
        	else 
        		return bundleRepo.getStockQuantity(product.getId());
        }else{
            return stock.getQuantity();
        }
    }



    /**
     * @return the sum of actual stock items quantities in the given list.
     * Bundles and services stock items are excluded.
     * */
    public Long getStockItemsQuantitySum(List<StocksEntity> stocks) {
        return stocks.stream()        		
                .filter(this::isPhysicalProduct)
                .mapToLong(stock -> stock.getQuantity())
                .sum();
    }
    
    
    
    
    public Boolean isPhysicalProduct(StocksEntity stock) {
    	return Optional.ofNullable(stock)
		    			.map(StocksEntity::getProductVariantsEntity)
		    			.map(ProductVariantsEntity::getProductEntity)
		    			.filter(product -> Objects.equals( product.getProductType(), ProductTypes.STOCK_ITEM) )
		    			.isPresent();
    }


    

	@Override
	public List<StocksEntity> getVariantStockForShop(ProductVariantsEntity variant, Long shopId) {
		Long variantId = variant.getId();
		return getVariantStockForShop(variantId, shopId);
	}

	
	
	
	

	@Override
	public List<StocksEntity> getVariantStockForShop(Long variantId, Long shopId) {
		List<StocksEntity> stocks  = new ArrayList<>();
		if(shopId != null) {
			stocks  = stockRepo.findByShopsEntity_IdAndProductVariantsEntity_Id(shopId, variantId);
		}else {
			stocks  = stockRepo.findByProductVariantsEntity_Id(variantId);
		}
		
        if(stocks == null )
        	stocks  = new ArrayList<>();

        stocks.stream().forEach(this::updateStockQuantity);

        return stocks;
	}
	
	
	
	
	public StockUpdateResponse updateStock(StockUpdateDTO stockUpdateReq) throws BusinessException {
		Long id = updateStockBatch(asList(stockUpdateReq))
					.stream()
					.findFirst()
					.orElseThrow(() -> 
						new BusinessException(
								format("Stock update Failed for [%s]!", stockUpdateReq.toString())
								, "OPERATION FAILURE"
								, NOT_ACCEPTABLE));
		return new StockUpdateResponse(id);
	}
	
	
	
	
	
	private Long saveStock(StockUpdateDTO stockUpdateReq, VariantCache variantCache, Map<Long,ShopsEntity> shopCache, VariantStockCache stockCache) {
		try {
			validateStockToUpdate(stockUpdateReq, variantCache, shopCache);
		} catch (BusinessException e) {
			throw new RuntimeBusinessException(e);
		}		
		return saveStockToDB(stockUpdateReq, variantCache, shopCache);
	}
	
	
	
	public List<Long> updateStockBatch(List<StockUpdateDTO> stocks) throws BusinessException{
		List<VariantIdentifier> variantIdentifiers = 
				stocks
				.stream()
				.map(this::getVariantIdentifier)
				.collect(toList());
		VariantCache variantCache = cachingHelper.createVariantCache(variantIdentifiers);
		
		List<Long> shopIdList = 
				stocks
				.stream()
				.map(StockUpdateDTO::getShopId)
				.collect(toList());		 
		Map<Long, ShopsEntity> shopCache = 
				shopRepo
				.findByIdIn(shopIdList)
				.stream()
				.collect(toMap(ShopsEntity::getId, shop -> shop));
		
		
		List<Long> variantIdList = 
				stocks
				.stream()
				.map(StockUpdateDTO::getVariantId)
				.collect(toList());
		List<StocksEntity> variantStocks = stockRepo.findByProductVariantsEntity_IdIn(variantIdList);
				
		VariantStockCache stockCache = new VariantStockCache(variantStocks);
		return stocks
				.stream()
				.map(stk -> saveStock(stk, variantCache, shopCache, stockCache))
				.collect(toList());
	}
	
	
	
	private VariantIdentifier getVariantIdentifier(StockUpdateDTO stock) {
		String idStr = ofNullable(stock)
						.map(StockUpdateDTO::getVariantId)
						.map(String::valueOf)
						.orElse(null);
		return new VariantIdentifier(idStr, null, null);
	}	
	
	

	private Long saveStockToDB(StockUpdateDTO req
			, VariantCache variantCache, Map<Long, ShopsEntity> shopCache) {
		Long shopId = req.getShopId();
		Long variantId = req.getVariantId();
		
		StocksEntity stock = getStockEntityToUpdate(shopId, variantId, variantCache, shopCache);
		
		if(req.getQuantity() != null) {
			stock.setQuantity( req.getQuantity() );
		}
		
		if(req.getPrice() != null) {
			stock.setPrice( req.getPrice() );
		}
		
		if(req.getCurrency() != null) {
			TransactionCurrency currecny = TransactionCurrency.getTransactionCurrency( req.getCurrency());
			stock.setCurrency( currecny );
		}
		
//		Long orgId = security.getCurrentUserOrganizationId();
//		SQLQueryFactory query = new SQLQueryFactory(createQueryDslConfig() , dataSource);
//		QStocks stk = QStocks.stocks;
//		query.insert(stk).values(stk.organizationId.eq(orgId), stk.currency.eq);
		
		stock = stockRepo.save(stock);
		
		return stock.getId();
	}

	
	
	
	private StocksEntity getStockEntityToUpdate(Long shopId, Long variantId, VariantCache variantCache, Map<Long, ShopsEntity> shopCache) {
		return stockRepo
				.findByProductVariantsEntity_IdAndShopsEntity_Id(variantId, shopId)
				.orElse(createNewStock(shopId, variantId, variantCache, shopCache));
	}
	
	
	
	

	private StocksEntity createNewStock(Long shopId, Long variantId, VariantCache variantCache, Map<Long, ShopsEntity> shopCache) {
		ShopsEntity shop =  shopCache.get(shopId);
//		VariantBasicData variant = variantCache.getIdToVariantMap().get(String.valueOf(variantId));
		OrganizationEntity organizationEntity = security.getCurrentUserOrganization();
		
		ProductVariantsEntity variant = new ProductVariantsEntity();
		variant.setId(variantId);
		StocksEntity stock = new StocksEntity();			
		stock.setShopsEntity(shop);
		stock.setProductVariantsEntity(variant);
		stock.setOrganizationEntity(organizationEntity);
		return stock;
	}



	private void validateStockToUpdate(StockUpdateDTO req
			, VariantCache variantCache, Map<Long, ShopsEntity> shopCache) throws BusinessException {
		if(!allParamExists(req) ){
			throw new BusinessException(
					format(ERR_MISSING_STOCK_UPDATE_PARAMS, req)
					, "MISSING_PARAM" 
					, NOT_ACCEPTABLE);
		}
		
		//previous validations should have checked that either quantity or (price-currency) were provided
		if(req.getQuantity() != null) {
			validateStockQuantity(req);
		}
		
		if(req.getPrice() != null && req.getCurrency() != null) {
			validateStockCurrency(req);		
			validateStockPrice(req);
		}
		
		validateShopId(req, shopCache);
		validateVariantId(req, variantCache);
	}
	
	

	
	
	private void validateVariantId(StockUpdateDTO req, VariantCache variantCache) throws BusinessException{
		Long id = req.getVariantId();
		if(!variantCache.getIdToVariantMap().containsKey(String.valueOf(id)) ) {
			throw new BusinessException(
					format("No product variant exists with id[%d]!", id)
					, "INVALID_PARAM:variant_id" 
					, NOT_ACCEPTABLE);
		}
		
		VariantBasicData variant = variantCache.getIdToVariantMap().get(String.valueOf(id));
		Long userOrgId = security.getCurrentUserOrganizationId();
		Long variantOrgId = variant.getOrganizationId(); 
		if(!Objects.equals(userOrgId, variantOrgId)) {
			throw new BusinessException(
					format("User from organization[%d] cannot change stock for variant of id[%d]!", userOrgId,  id)
					, "INVALID_PARAM:variant_id" 
					, FORBIDDEN);
		}
	}
	
	
	

	private void validateShopId(StockUpdateDTO req, Map<Long, ShopsEntity> shopCache) throws BusinessException{
		validateShopExists(req, shopCache);
		ShopsEntity shop = shopCache.get(req.getShopId());
		validateIfOrgMgrOfShop(shop);
		validateIfStoreManagerOfShop(shop);
	}

	
	
	


	private void validateShopExists(StockUpdateDTO req, Map<Long, ShopsEntity> shopCache) throws BusinessException {
		Long shopId = req.getShopId();		
		if(!shopCache.containsKey(shopId)) {
			throw new BusinessException(
					format("No shop exists with id[%d]!", shopId)
					, "INVALID_PARAM:shop_id" 
					, NOT_ACCEPTABLE);
		}
	}



	
	
	
	private void validateIfOrgMgrOfShop(ShopsEntity shop) throws BusinessException {
		Long shopOrgId = shop.getOrganizationEntity().getId();
		Long userOrgId = security.getCurrentUserOrganizationId();
		if(!Objects.equals( shopOrgId, userOrgId )) {
			throw new BusinessException(
					format("User from organization with id[%d] cannot change a shop from "
							+ "organization with id[%d]!"
							, userOrgId, shopOrgId)
					, "INVALID_PARAM:shop_id" 
					, FORBIDDEN);
		}
	}



	
	
	private void validateIfStoreManagerOfShop(ShopsEntity shop) throws BusinessException {
		BaseUserEntity user = security.getCurrentUser();
		if(!(user instanceof EmployeeUserEntity)) {
			throw new BusinessException("User is not an Employee!", "INVALID_OPERATION", FORBIDDEN);
		}
		EmployeeUserEntity empUser = (EmployeeUserEntity)user;
		
		//TODO: revise this condition
		if( !security.currentUserHasRole(ORGANIZATION_MANAGER) 
				&& security.currentUserHasRole(STORE_MANAGER)
				&& !Objects.equals( empUser.getShopId(), shop.getId()) ) {
			throw new BusinessException(
					String.format("Shop Manager of shop with id[%d] cannot make changes "
							+ "in another shop with id[%d]!"
							,  empUser.getShopId(), shop.getId())
					, "INVALID_PARAM:shop_id" 
					, FORBIDDEN);
		}
	}
	
	
	
	

	private void validateStockQuantity(StockUpdateDTO req) throws BusinessException {
		Integer quantity = req.getQuantity();
		if( quantity != null &&  quantity.intValue() < 0) {
			throw new BusinessException(
					String.format("Invalid Quantity value [%d]!", quantity)
					, "INVALID_PARAM:quantity" 
					, NOT_ACCEPTABLE);
		}		
	}
	
	
	
	
	

	private void validateStockPrice(StockUpdateDTO req) throws BusinessException {
		BigDecimal price = req.getPrice();
		if( price != null &&  price.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException(
					String.format("Invalid Price value [%s]!", price.toString())
					, "INVALID_PARAM:currency" 
					, NOT_ACCEPTABLE);
		}
	}
	
	
	
	
	

	private void validateStockCurrency(StockUpdateDTO req) throws BusinessException {
		Integer currency = req.getCurrency();
		boolean invalidCurrency = asList( TransactionCurrency.values() )
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
	
	
	
	

	private boolean allParamExists(StockUpdateDTO req) {
		//y = BC + AB'C' -> boolean equation that checks if quantity, price, currency exists as per business rules.
		Boolean A = req.getQuantity() != null;
		Boolean B = req.getPrice() != null;
		Boolean C = req.getCurrency() != null;
		
		return !anyIsNull( req.getShopId() ,req.getVariantId() )
									&&  ( (B && C) || (A && !B && !C) );
	}
	
	
	
	private Configuration createQueryDslConfig() {
		Configuration config = new Configuration(new PostgreSQLTemplates());
		config.setUseLiterals(true);
		return config;
	}
}









@Data
class VariantStockCache{
	private Map<Long, List<StocksEntity>> variantStocks;
	
	VariantStockCache(List<StocksEntity> stocks){
		this.variantStocks = 
			ofNullable(stocks)
			.orElse(emptyList())
			.stream()
			.collect(groupingBy(this::stockVariantId));
	}
	
	
	private Long stockVariantId(StocksEntity stk) {
		return ofNullable(stk).map(StocksEntity::getProductVariantsEntity).map(ProductVariantsEntity::getId).orElse(-1L);
	}
	
	public List<StocksEntity> getVariantStocks(Long variantId){
		return ofNullable(variantId)
				.map(variantStocks::get)
				.orElse(emptyList());
	}
}
