package com.nasnav.service;

import static com.nasnav.persistence.EntityUtils.anyIsNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.StockUpdateResponse;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private BundleRepository bundleRepo;
    
    @Autowired
    private ProductVariantsRepository variantRepo;    
    
    @Autowired
    private RoleRepository roleRepo;
    
    @Autowired 
    private OrganizationRepository orgRepo;

    @Autowired
    private EmployeeUserRepository empUserRepo;    
    
    @Autowired
    private ShopsRepository shopRepo;


    public List<StocksEntity> getProductStockForShop(Long productId, Long shopId) throws BusinessException {
        Optional<ProductEntity> prodOpt = productRepo.findById(productId);
        
        if(prodOpt == null || !prodOpt.isPresent())
            throw new BusinessException(
            		String.format("No product exists with id [%d]!",productId)
            		, "INVALID PARAM:product_id"
            		, HttpStatus.NOT_ACCEPTABLE);

        List<StocksEntity> stocks  = stockRepo.findByProductIdAndShopsId(productId, shopId);;

        if(stocks == null || stocks.isEmpty())
        	throw new BusinessException(
            		String.format("Product with id [%d] has no stocks!",productId)
            		, "INVALID PARAM:product_id"
            		, HttpStatus.NOT_ACCEPTABLE);

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
	public List<StocksEntity> getVariantStockForShop(ProductVariantsEntity variant, Long shopId) throws BusinessException {
		List<StocksEntity> stocks  = new ArrayList<>();
		if(shopId != null) {
			stocks  = stockRepo.findByShopsEntity_IdAndProductVariantsEntity_Id(shopId, variant.getId());
		}else {
			stocks  = stockRepo.findByProductVariantsEntity_Id(variant.getId());
		}
        

        if(stocks == null || stocks.isEmpty())
        	throw new BusinessException(
            		String.format("Product Variant with id [%d] has no stocks!", variant.getId())
            		, "INVALID PARAM:product_id"
            		, HttpStatus.NOT_ACCEPTABLE);

        stocks.stream().forEach(this::updateStockQuantity);

        return stocks;
	}
	
	
	
	
	public StockUpdateResponse updateStock(StockUpdateDTO stockUpdateReq) throws BusinessException {
		validateStockToUpdate(stockUpdateReq);
			
		return saveStockToDB(stockUpdateReq);
	}
	
	
	
	

	private StockUpdateResponse saveStockToDB(StockUpdateDTO req) {
		Long shopId = req.getShopId();
		Long variantId = req.getVariantId();
		
		StocksEntity stock = getStockEntityToUpdate(shopId, variantId);
		
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
		
		stock = stockRepo.save(stock);
		
		return new StockUpdateResponse(stock.getId());
	}

	
	
	
	private StocksEntity getStockEntityToUpdate(Long shopId, Long variantId) {
		Optional<StocksEntity> optional = stockRepo.findByProductVariantsEntity_IdAndShopsEntity_Id(variantId, shopId);
		StocksEntity stock;
		if(optional.isPresent()) {
			stock = optional.get();
		}else {
			ShopsEntity shop = shopRepo.findById(shopId).get();
			ProductVariantsEntity variant = variantRepo.findById(variantId).get();
			
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			EmployeeUserEntity user =  empUserRepo.getOneByEmail(auth.getName());
			OrganizationEntity organizationEntity = orgRepo.findById( user.getOrganizationId() ).get();
			
			stock = new StocksEntity();			
			stock.setShopsEntity(shop);
			stock.setProductVariantsEntity(variant);
			stock.setOrganizationEntity(organizationEntity);
		}
		return stock;
	}
	
	
	
	

	private void validateStockToUpdate(StockUpdateDTO req) throws BusinessException {
		if(!allParamExists(req) ){
			throw new BusinessException(
					"Missing required parameters! required parameters are {shop_id, variant_id, [quantity OR price and currency]}"
					, "MISSING_PARAM" 
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		EmployeeUserEntity user =  empUserRepo.getOneByEmail(auth.getName());
		
		//previous validations should have checked that either quantity or (price-currency) were provided
		if(req.getQuantity() != null) {
			validateStockQuantity(req);
		}
		
		if(req.getPrice() != null && req.getCurrency() != null) {
			validateStockCurrency(req);		
			validateStockPrice(req);
		}
		
		validateShopId(req, user);
		validateVariantId(req, user);
	}
	
	

	
	
	private void validateVariantId(StockUpdateDTO req, EmployeeUserEntity user) throws BusinessException{
		Long id = req.getVariantId();
		if(!variantRepo.existsById(id) ) {
			throw new BusinessException(
					String.format("No product variant exists with id[%d]!", id)
					, "INVALID_PARAM:variant_id" 
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	

	private void validateShopId(StockUpdateDTO req, EmployeeUserEntity user) throws BusinessException{
		Long shopId = validateShopExists(req);		
		validateIfOrgMgrOfShop(user, shopId);
		validateIfStoreManagerOfShop(user, shopId);
	}

	
	
	


	private Long validateShopExists(StockUpdateDTO req) throws BusinessException {
		Long shopId = req.getShopId();
		if(!shopRepo.existsById(shopId)) {
			throw new BusinessException(
					String.format("No shop exists with id[%d]!", shopId)
					, "INVALID_PARAM:shop_id" 
					, HttpStatus.NOT_ACCEPTABLE);
		}
		return shopId;
	}



	
	
	
	private void validateIfOrgMgrOfShop(EmployeeUserEntity user, Long shopId) throws BusinessException {
		ShopsEntity shop = shopRepo.findById(shopId).get();
		Long shopOrgId = shop.getOrganizationEntity().getId();
		Long userOrgId = user.getOrganizationId();
		if(!Objects.equals( shopOrgId, userOrgId )) {
			throw new BusinessException(
					String.format("User from organization with id[%d] cannot change a shop from "
							+ "organization with id[%d]!"
							, userOrgId, shopOrgId)
					, "INVALID_PARAM:shop_id" 
					, HttpStatus.FORBIDDEN);
		}
	}



	
	
	private void validateIfStoreManagerOfShop(EmployeeUserEntity user, Long shopId) throws BusinessException {
		List<Roles> userRoles = roleRepo.getRolesOfEmployeeUser(user.getId())
											.stream()
											.map(ent -> Roles.fromString(ent.getName()))
											.collect(Collectors.toList());
		
		if( !userRoles.contains(Roles.ORGANIZATION_MANAGER) 
				&& userRoles.contains( Roles.STORE_MANAGER )
				&& !Objects.equals( user.getShopId(), shopId) ) {
			throw new BusinessException(
					String.format("Shop Manager of shop with id[%d] cannot make changes "
							+ "in another shop with id[%d]!"
							,  user.getShopId(), shopId)
					, "INVALID_PARAM:shop_id" 
					, HttpStatus.FORBIDDEN);
		}
	}
	
	
	
	

	private void validateStockQuantity(StockUpdateDTO req) throws BusinessException {
		Integer quantity = req.getQuantity();
		if( quantity != null &&  quantity.intValue() < 0) {
			throw new BusinessException(
					String.format("Invalid Quantity value [%d]!", quantity)
					, "INVALID_PARAM:quantity" 
					, HttpStatus.NOT_ACCEPTABLE);
		}		
	}
	
	
	
	
	

	private void validateStockPrice(StockUpdateDTO req) throws BusinessException {
		BigDecimal price = req.getPrice();
		if( price != null &&  price.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException(
					String.format("Invalid Price value [%s]!", price.toString())
					, "INVALID_PARAM:currency" 
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	
	
	

	private void validateStockCurrency(StockUpdateDTO req) throws BusinessException {
		Integer currency = req.getCurrency();
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
	
	
	
	

	private boolean allParamExists(StockUpdateDTO req) {
		//y = BC + AB'C' -> boolean equation that checks if quantity, price, currency exists as per business rules.
		Boolean A = req.getQuantity() != null;
		Boolean B = req.getPrice() != null;
		Boolean C = req.getCurrency() != null;
		
		return !anyIsNull( req.getShopId() ,req.getVariantId() )
									&&  ( (B && C) || (A && !B && !C) );
	}
}
