package com.nasnav.service;

import static com.nasnav.commons.utils.CollectionUtils.divideToBatches;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_MISSING_STOCK_UPDATE_PARAMS;
import static com.nasnav.enumerations.Roles.ORGANIZATION_MANAGER;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.persistence.ProductTypes.BUNDLE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.commons.model.IndexedData;
import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.StockValidationException;
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
    private CachingHelper cachingHelper;
    
    @Autowired
    private EntityManager entityManager;

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
        ProductEntity product = ofNullable(stock.getProductVariantsEntity())
        								.map(ProductVariantsEntity::getProductEntity)
        								.orElse(null);
        if(product == null){
            return stock.getQuantity();
        }

        Integer productType = product.getProductType();

        if( productType.equals(BUNDLE) ){
        	if(stock.getQuantity().equals(0))
        		return 0;
        	else 
        		return bundleRepo.getStockQuantity(product.getId());
        }else{
            return stock.getQuantity();
        }
    }



    
    
    public Boolean isPhysicalProduct(StocksEntity stock) {
    	return ofNullable(stock)
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
	
	
	
	
	
	private StocksEntity prepareStockEntity(IndexedData<StockUpdateDTO> indexedStkDto, VariantCache variantCache, Map<Long,ShopsEntity> shopCache, VariantStockCache stockCache) {
		StockUpdateDTO stockUpdateReq = indexedStkDto.getData();
		try {
			validateStockToUpdate(stockUpdateReq, variantCache, shopCache);
		} catch (BusinessException e) {
			throw new StockValidationException(e, indexedStkDto.getIndex());
		}
		
		Long shopId = stockUpdateReq.getShopId();
		Long variantId = stockUpdateReq.getVariantId();
		
		StocksEntity stock = getStockEntityToUpdate(shopId, variantId, variantCache, shopCache, stockCache);
		
		if(stockUpdateReq.getQuantity() != null) {
			stock.setQuantity( stockUpdateReq.getQuantity() );
		}
		
		if(stockUpdateReq.getPrice() != null) {
			stock.setPrice( stockUpdateReq.getPrice() );
		}
		
		if(stockUpdateReq.getCurrency() != null) {
			TransactionCurrency currecny = TransactionCurrency.getTransactionCurrency( stockUpdateReq.getCurrency());
			stock.setCurrency( currecny );
		}
		
		if(stockUpdateReq.getDiscount() != null) {
			stock.setDiscount( stockUpdateReq.getDiscount() );
		}

		if(stockUpdateReq.getUnit() != null) {
			stock.setUnit(stockUpdateReq.getUnit());
		}

		return stock;
	}
	
	
	
	
	@Override
	@Transactional(rollbackOn = Throwable.class)	
	public List<Long> updateStockBatch(List<StockUpdateDTO> stocks){
		VariantCache variantCache = createVariantsCache(stocks);
		return updateStockBatch(stocks, variantCache);
	}
	
	
	
	
	@Override
	@Transactional(rollbackOn = Throwable.class)	
	public List<Long> updateStockBatch(List<StockUpdateDTO> stocks, VariantCache variantCache){
		Map<Long, ShopsEntity> shopCache = createShopsCache(stocks);		
		VariantStockCache stockCache = createStocksCache(stocks);
		
		List<StocksEntity> stocksToUpdate = prepareStocksToUpdate(stocks, variantCache, shopCache, stockCache);
		
		return saveAllStocks(stocksToUpdate);
	}

	


	private List<Long> saveAllStocks(List<StocksEntity> stocksToUpdate) {
		return StreamSupport
				.stream(stockRepo.saveAll(stocksToUpdate).spliterator(), false)
				.map(StocksEntity::getId)
				.collect(toList());
	}


	

	private List<StocksEntity> prepareStocksToUpdate(List<StockUpdateDTO> stocks, VariantCache variantCache,
			Map<Long, ShopsEntity> shopCache, VariantStockCache stockCache) {
		Set<String> seen = new HashSet<>();
		return IntStream
				.range(0, stocks.size())
				.mapToObj(i -> new IndexedData<>(i, stocks.get(i)))
				.filter(stk -> isNotSeenBefore(seen, stk))
				.map(stk -> prepareStockEntity(stk, variantCache, shopCache, stockCache))
				.collect(toList());
	}



	private boolean isNotSeenBefore(Set<String> seen, IndexedData<StockUpdateDTO> stk) {
		String key = variantIdAndShopIdCombination(stk.getData());
		return seen.add(key);
	}

	
	
	
	private String variantIdAndShopIdCombination(StockUpdateDTO stk) {
		Long variantId = ofNullable(stk).map(StockUpdateDTO::getVariantId).orElse(-1L);
		Long shopId = ofNullable(stk).map(StockUpdateDTO::getShopId).orElse(-1L);
		return format("%d-%d", variantId, shopId);
	}


	private VariantStockCache createStocksCache(List<StockUpdateDTO> stocks) {
		Set<Long> variantIdList = 
				stocks
				.stream()
				.map(StockUpdateDTO::getVariantId)
				.collect(toSet());
		
		List<StocksEntity> variantStocks = 
				divideToBatches(variantIdList, 500)
					.stream()
					.map(stockRepo::findByProductVariantsEntity_IdIn)
					.flatMap(List::stream)
					.collect(toList());
				
		VariantStockCache stockCache = new VariantStockCache(variantStocks);
		return stockCache;
	}



	private Map<Long, ShopsEntity> createShopsCache(List<StockUpdateDTO> stocks) {
		Set<Long> shopIdList = 
				stocks
				.stream()
				.map(StockUpdateDTO::getShopId)
				.collect(toSet());		 
		Map<Long, ShopsEntity> shopCache = 
				shopRepo
				.findByIdInAndRemoved(shopIdList, 0)
				.stream()
				.collect(toMap(ShopsEntity::getId, shop -> shop));
		return shopCache;
	}



	private VariantCache createVariantsCache(List<StockUpdateDTO> stocks) {
		List<VariantIdentifier> variantIdentifiers = 
				stocks
				.stream()
				.map(this::getVariantIdentifier)
				.collect(toList());
		VariantCache variantCache = cachingHelper.createVariantCache(variantIdentifiers);
		return variantCache;
	}
	
	
	
	
	
	private VariantIdentifier getVariantIdentifier(StockUpdateDTO stock) {
		String idStr = ofNullable(stock)
						.map(StockUpdateDTO::getVariantId)
						.map(String::valueOf)
						.orElse(null);
		return new VariantIdentifier(idStr, null, null);
	}	
	
	

	
	
	
	private StocksEntity getStockEntityToUpdate(Long shopId, Long variantId, VariantCache variantCache, Map<Long, ShopsEntity> shopCache,VariantStockCache stockCache) {
		return stockCache
				.getVariantStocks(variantId)
				.stream()
				.filter(stk -> hasShopIdOf(stk, shopId))
				.findFirst()
				.orElse(createNewStock(shopId, variantId, variantCache, shopCache));
	}
	
	
	
	
	private boolean hasShopIdOf(StocksEntity stock , Long shopId) {
		return ofNullable(stock)
				.map(StocksEntity::getShopsEntity)
				.map(ShopsEntity::getId)
				.orElse(-1L)
				.equals(shopId);
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



	@Override
	public void reduceStockBy(StocksEntity stocksEntity, Integer quantity) {
		if(isBundleStock(stocksEntity)) {
			reduceBundleStock(stocksEntity, quantity);
		}else {
			reduceNormalStockBy(stocksEntity, quantity);
		}
	}
	
	
	
	@Override
	public void incrementStockBy(StocksEntity stocksEntity, Integer quantity) {
		if(isBundleStock(stocksEntity)) {
			incrementBundleStock(stocksEntity, quantity);
		}else {
			incrementNormalStockBy(stocksEntity, quantity);
		}
	}



	private void reduceBundleStock(StocksEntity stocksEntity, Integer quantity) {
		stockRepo
			.findByBundleStockId(stocksEntity.getId())
			.forEach(itemStock -> incrementNormalStockBy(itemStock, quantity));  
	}
	
	
	
	private void incrementBundleStock(StocksEntity stocksEntity, Integer quantity) {
		stockRepo
			.findByBundleStockId(stocksEntity.getId())
			.forEach(itemStock -> reduceNormalStockBy(itemStock, quantity));  
	}



	private boolean isBundleStock(StocksEntity stocksEntity) {
		return stockRepo
				.getStockProductType(stocksEntity.getId())
				.map(productType -> Objects.equals(productType, BUNDLE))
				.orElse(false);
	}



	private StocksEntity reduceNormalStockBy(StocksEntity stocksEntity, Integer quantity) {
		int newQuantity = stocksEntity.getQuantity() - ofNullable(quantity).orElse(0).intValue();
		stocksEntity.setQuantity(newQuantity);
		return stockRepo.save(stocksEntity);
	}
	
	
	
	
	private StocksEntity incrementNormalStockBy(StocksEntity stocksEntity, Integer quantity) {
		int newQuantity = stocksEntity.getQuantity() + ofNullable(quantity).orElse(0).intValue();
		stocksEntity.setQuantity(newQuantity);
		return stockRepo.save(stocksEntity);
	}



	@Override
	public void updateStockQuantity(StockUpdateDTO updateDto) {
		Query query = entityManager.createQuery(
						"UPDATE StocksEntity stock SET quantity = :quantity "
						+ " WHERE stock.shopsEntity.id = :shopId "
						+ " AND stock.productVariantsEntity.id = :variantId");
		query
		.setParameter("quantity", updateDto.getQuantity())
		.setParameter("shopId", updateDto.getShopId())
		.setParameter("variantId", updateDto.getVariantId())
		.executeUpdate();
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
