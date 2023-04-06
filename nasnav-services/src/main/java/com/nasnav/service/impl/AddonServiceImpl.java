package com.nasnav.service.impl;

import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.commons.utils.EntityUtils.copyNonNullProperties;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.AddonStockRepository;
import com.nasnav.dao.AddonsRepository;
import com.nasnav.dao.CartItemAddonDetailsRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductsCustomRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.AddonDetailsDTO;
import com.nasnav.dto.AddonStockDTO;
import com.nasnav.dto.AddonStocksDTO;
import com.nasnav.dto.AddonsDTO;
import com.nasnav.dto.Pair;
import com.nasnav.dto.ProductAddonDTO;
import com.nasnav.dto.ProductAddonsDTO;
import com.nasnav.enumerations.AddonType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddonEntity;
import com.nasnav.persistence.AddonStocksEntity;
import com.nasnav.persistence.CartItemAddonDetailsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.dto.query.result.products.ProductAddonBasicData;
import com.nasnav.response.AddonResponse;
import com.nasnav.service.AddonService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.ProductAddonPair;

@Service
public class AddonServiceImpl implements AddonService {
	Logger logger = LogManager.getLogger(getClass());
	@Autowired
    private AddonsRepository addonRepo;
	@Autowired
	private SecurityService securityService;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	CartItemAddonDetailsRepository cartItemAddonDetailsRepository;

	 
	@Autowired
	private ProductsCustomRepository productsCustomRepo;
	
	@Autowired
	AddonStockRepository addonStockRepo;
	
	@Autowired
	ShopsRepository shopsRepo;
	    @Override
			public AddonEntity createOrUpdateAddon(AddonsDTO addonDTO)  throws BusinessException{
	    	validateAddonDto(addonDTO);
	        
	        String operation = addonDTO.getOperation();
	        
	        AddonEntity entity = null;
	        if(Objects.equals(operation, "create")) {
	        	entity = createNewAddon(addonDTO);
	        }else {
	        	entity = updateAddon(addonDTO);
	        }    

	        return entity;
	    }

	
	   private AddonEntity createNewAddon(AddonsDTO addonDTO) {
	    	OrganizationEntity org = securityService.getCurrentUserOrganization();
	    	  Integer type =
	                  ofNullable(addonDTO.getType())
	                          .map(AddonType::getAddonsType)
	                          .orElse(AddonType.EXTRA)
	                          .getValue();
	    	AddonEntity entity = new AddonEntity();
	        entity.setOrganizationEntity(org);
	        
	        entity.setName(addonDTO.getName());
	        entity.setType(type);

	        return addonRepo.save(entity);
	    }
	   private AddonEntity updateAddon(AddonsDTO addonDTO) throws BusinessException {
		  
			OrganizationEntity org = securityService.getCurrentUserOrganization();
			AddonEntity entity = addonRepo
						.findByIdAndOrganizationEntity_Id(addonDTO.getId(), org.getId())
						.orElseThrow(() -> new BusinessException(
												"INVALID PARAM: id"
												, "No addon exists in the organization with provided id"
												, NOT_ACCEPTABLE));
			
			try {
				copyNonNullProperties(addonDTO, entity);			
				
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				logger.error(e, e);
				throw new BusinessException(
						format("Failed to update addon [%s]!", addonDTO.toString())
						, "INTERNAL FAILURE"
						, INTERNAL_SERVER_ERROR);
			}

			
			return addonRepo.save(entity);
	    }
	   
	   
	   private void validateAddonDto(AddonsDTO addonDTO) throws BusinessException {
			String operation = addonDTO.getOperation();

	        
	        if( isBlankOrNull(operation)) {
	        	throw new BusinessException("MISSING PARAM: operation", "", NOT_ACCEPTABLE);
	        }else if(Objects.equals(operation, "create")) {
	        	 if (isBlankOrNull(addonDTO.getName())) {
	            	throw new BusinessException("MISSING PARAM: name", "name is required to create addon", NOT_ACCEPTABLE);
	            }
	            
	        }else if(Objects.equals(operation, "update")) {
				 if (addonDTO.getId() == null) {
					 throw new BusinessException("MISSING PARAM: id", "id is required to update addon", NOT_ACCEPTABLE);
				 }  
				
	        }else {
	            throw new BusinessException("INVALID PARAM: operation", "unsupported operation" + addonDTO.getOperation(), NOT_ACCEPTABLE);
	        }
		}

	   @Override
		public List<AddonEntity>  findAllAddonPerOrganization(){
		   Long orgId = securityService.getCurrentUserOrganizationId();
		   return addonRepo.findByOrganizationEntity_Id(orgId);
	   }

	   @Override
		public AddonResponse deleteOrgAddon(Long addonId) throws BusinessException {
	        Long orgId = securityService.getCurrentUserOrganizationId();
	    	AddonEntity addon = addonRepo
					.findByIdAndOrganizationEntity_Id(addonId, orgId)
					.orElseThrow(() -> new BusinessException(
											"INVALID PARAM: id"
											, "No addon exists in the organization with provided id"
											, NOT_ACCEPTABLE));
	       if(!addonStockRepo.existsByAddonEntity_Id(addonId)) {
	            
	    	productRepository.detachProductsFromAddon(addonId);
	    	addonRepo.delete(addon);
	    	
	       }else{
	    	   throw new BusinessException("",
                                "this addon has stocks ,can't be deleted"
						, NOT_ACCEPTABLE); 
	       }
            
	        return new AddonResponse(addon.getId());
	    }
	   
	   



		@Override
		public boolean updateProductAddons(ProductAddonDTO productAddonDTO) throws BusinessException {
			validateProductAddonDTO(productAddonDTO.getProductIds(), productAddonDTO.getAddonIds());

			List<Long> productIds = productAddonDTO.getProductIds();
			List<Long> addonsIds = productAddonDTO.getAddonIds();

			Set<ProductAddonPair> newProductAddons= createProductAddonPairs(productIds, addonsIds);

			addAddonsToProducts(newProductAddons);

			return true;
		}



		private Set<ProductAddonPair> createProductAddonPairs(List<Long> productIds, List<Long> addonsIds) {
			return productIds
					.parallelStream()
					.flatMap(id -> getProductAddonPairs(id, addonsIds))
					.distinct()
					.collect(toSet());
		}



		@Override
		public void addAddonsToProducts(Set<ProductAddonPair> newProductAddons) {
			Set<Long> prodIds = getProductIds(newProductAddons);
			Set<Long> addonsIds = getAddonIds(newProductAddons);

			validateProductIdsExists(prodIds);
			validateAddonIdsExists(addonsIds);

			Set<ProductAddonPair> existingProductAddons = getExistingProductAddons(prodIds);

			batchInsertProductAddonsToDB(newProductAddons, existingProductAddons);
		}

		private void validateProductIdsExists(Set<Long> productIds) {
			Long orgId = securityService.getCurrentUserOrganizationId();
			Collection<Long> batch =  mapInBatches(productIds, 5000, p -> productRepository.getExistingProductIds(new HashSet<>(p), orgId));
			Set<Long> existingIds = new HashSet<>(batch);
			productIds
					.stream()
					.filter(id -> !existingIds.contains(id))
					.findFirst()
					.ifPresent(nonExistingId ->
					{throw new RuntimeBusinessException(
							format("Provided product_id(%d) does't match any existing product", nonExistingId)
							, "INVALID PARAM:product_id"
							, NOT_ACCEPTABLE);});
		}

		private void validateAddonIdsExists(Set<Long> addonsIds) {
			Long orgId = securityService.getCurrentUserOrganizationId();
			Collection<Long> batch = mapInBatches(addonsIds, 500, t -> addonRepo.getExistingAddonsIds(addonsIds, orgId));
			Set<Long> existingIds = new HashSet<>(batch);
			addonsIds
					.stream()
					.filter(id -> !existingIds.contains(id))
					.findFirst()
					.ifPresent(nonExistingId ->
					{throw new RuntimeBusinessException(
							format("Provided addon(%d) does't match any addon", nonExistingId)
							, "INVALID PARAM:product_id"
							, NOT_ACCEPTABLE);});
		}


		private void batchInsertProductAddonsToDB(Set<ProductAddonPair> newProductAddons, Set<ProductAddonPair> existingProductAddons) {

			Set<ProductAddonPair> validProductAddon =
					ofNullable(newProductAddons)
							.orElse(emptySet())
							.stream()
							.filter(pair -> !existingProductAddons.contains(pair))
							.filter(this::isValidProductAddonPair)
							.collect(toSet());

			productsCustomRepo.batchInsertProductAddons(validProductAddon);
		}








		private boolean isValidProductAddonPair(ProductAddonPair pair) {
			return noneIsNull(pair, pair.getProductId(), pair.getAddonId());
		}







		private Set<Long> getProductIds(Set<ProductAddonPair> newProductAddons) {
			return newProductAddons
					.parallelStream()
					.map(ProductAddonPair::getProductId)
					.collect(toSet());
		}



		private Set<Long> getAddonIds(Set<ProductAddonPair> newProductAddons) {
			return newProductAddons
					.parallelStream()
					.map(ProductAddonPair::getAddonId)
					.collect(toSet());
		}



		private Set<ProductAddonPair> getExistingProductAddons(Set<Long> prodIds) {
			return ofNullable(prodIds)
					.filter(EntityUtils::noneIsEmpty)
					.map(ids -> mapInBatches(ids, 5000, addonRepo::getAddonsByProductIdIn))
					.orElse(emptyList())
					.parallelStream()
					.map(this::toProdutAddonPair)
					.collect(toSet());
		}




		private ProductAddonPair toProdutAddonPair(ProductAddonBasicData basicData) {
			return new ProductAddonPair(basicData.getProductId(), basicData.getAddonId());
		}




		private Stream<ProductAddonPair> getProductAddonPairs(Long productId, List<Long> addonIds){
			return ofNullable(addonIds)
					.orElse(emptyList())
					.parallelStream()
					.map(addonId -> new ProductAddonPair(productId, addonId));
		}



		private void validateProductAddonDTO(List<Long> productIds, List<Long> addonsIds) throws BusinessException {
			if(isBlankOrNull(productIds))
				throw new BusinessException("Provided products_ids can't be empty", "MISSING PARAM:products_ids", NOT_ACCEPTABLE);

			if(isBlankOrNull(addonsIds))
				throw new BusinessException("Provided addons_ids can't be empty", "MISSING PARAM:addons_ids", NOT_ACCEPTABLE);
		}

		
		@Override
		public boolean deleteProductAddons(List<Long> productIds, List<Long> addonsIds) throws BusinessException {
			validateProductAddonDTO(productIds, addonsIds);

			Map<Long, ProductEntity> productsMap = validateAndGetProductMap(productIds);
			Map<Long, AddonEntity> addonsMap = validateAndGetAddonMap(addonsIds);

			List<Pair> productAddonsList = productRepository.getProductAddons(productIds, addonsIds);

			for(Long productId : productIds) {
				for(Long addonId : addonsIds) {
					if( productAddonsList.contains(new Pair(productId, addonId)))
						productsMap.get(productId).removeProductAddon(addonsMap.get(addonId));
					else
						throw new BusinessException("INVALID PARAM", "Link between product "+ productId +" and addon "+ addonId +" doesn't exist!", HttpStatus.NOT_ACCEPTABLE);
				}
				productRepository.save(productsMap.get(productId));
			}
			return true;
		}


	






		private Map<Long, ProductEntity> validateAndGetProductMap(List<Long> productIds) throws BusinessException {

			Map<Long, ProductEntity> productsMap =
					mapInBatches(productIds, 500, productRepository::findByIdIn)
							.stream()
							.collect(toMap(ProductEntity::getId, entity -> entity));

			for(Long productId : productIds) {
				if (productsMap.get(productId) == null)
					throw new BusinessException(
							format("Provided product_id(%d) does't match any existing product", productId)
							, "INVALID PARAM:product_id"
							, NOT_ACCEPTABLE);
			}

			return productsMap;
		}






		private Map<Long, AddonEntity> validateAndGetAddonMap(List<Long> addonsIds) throws BusinessException {
			Long orgId = securityService.getCurrentUserOrganizationId();

			Map<Long, AddonEntity> addonsMap =
					mapInBatches(addonsIds, 500, tgs -> addonRepo.findByIdInAndOrganizationEntity_Id(tgs, orgId))
							.stream()
							.collect(toMap(AddonEntity::getId, entity -> entity));

			for(Long addonId : addonsIds) {
				if (addonsMap.get(addonId) == null)
					throw new BusinessException(
							format("Provided addon_id(%d) doesn't match any existing addon for organization(%d)", addonId, orgId)
							,"INVALID PARAM:addon_id", NOT_ACCEPTABLE);
			}
			return addonsMap;
		}


	    @Override
			public AddonStocksEntity createOrUpdateAddonStock(AddonStockDTO addonStockDTO)  throws BusinessException{
	    	validateAddonStockDto(addonStockDTO);
	        
	        String operation = addonStockDTO.getOperation();
	        
	        AddonStocksEntity entity = null;
	        if(Objects.equals(operation, "create")) {
	        	entity = createNewAddonStock(addonStockDTO);
	        }else {
	       	entity = updateAddonStock(addonStockDTO);
	        }    

	        return entity;
	    }

	    private AddonStocksEntity createNewAddonStock(AddonStockDTO addonStockDTO) throws BusinessException {
	    	OrganizationEntity org = securityService.getCurrentUserOrganization();
	    	AddonEntity addon = addonRepo
					.findByIdAndOrganizationEntity_Id(addonStockDTO.getAddonId(), org.getId())
					.orElseThrow(() -> new BusinessException(
											"INVALID PARAM: id"
											, "No addon exists in the organization with provided id"
											, NOT_ACCEPTABLE));
	    	ShopsEntity shop = shopsRepo
						.findByIdAndOrganizationEntity_IdAndRemoved(addonStockDTO.getShopId(), org.getId(),0)
						.orElseThrow(() -> new BusinessException(
												"INVALID PARAM: id"
												, "No shop exists in the organization with provided id"
												, NOT_ACCEPTABLE));
	    	if(addonStockRepo.existsByShopsEntity_IdAndAddonEntity_Id(addonStockDTO.getShopId(), addonStockDTO.getAddonId())) {
	    		throw new BusinessException(
						""
						, "this stock  alrady exists in the shop with provided addon"
						, NOT_ACCEPTABLE);
	    	}
	    	AddonStocksEntity entity = new AddonStocksEntity();
	        entity.setPrice(addonStockDTO.getPrice());
	        entity.setQuantity(addonStockDTO.getQuantity());
	        entity.setAddonEntity(addon);
	        entity.setShopsEntity(shop);

	        return addonStockRepo.save(entity);
	    }
	    private AddonStocksEntity updateAddonStock(AddonStockDTO addonStockDTO) throws BusinessException {

	    	AddonStocksEntity entity = addonStockRepo
						.findByIdAndShopsEntity_IdAndAddonEntity_Id(addonStockDTO.getId(),addonStockDTO.getShopId(), addonStockDTO.getAddonId())
						.orElseThrow(() -> new BusinessException(
												"INVALID PARAM: id"
												, "No addon stock available with provided id"
												, NOT_ACCEPTABLE));
	    	
	        entity.setPrice(addonStockDTO.getPrice());
	        entity.setQuantity(addonStockDTO.getQuantity());
	    
	        return addonStockRepo.save(entity);
	    }
	    
	    
	    private void validateAddonStockDto(AddonStockDTO addonStockDTO) throws BusinessException {
			String operation = addonStockDTO.getOperation();

	        
	        if( isBlankOrNull(operation)) {
	        	throw new BusinessException("MISSING PARAM: operation", "", NOT_ACCEPTABLE);
	        }else if(Objects.equals(operation, "create")) {
	        	 if (isBlankOrNull(addonStockDTO.getAddonId())) {
	            	throw new BusinessException("MISSING PARAM: addonId ", "addonId is required to create addon stock", NOT_ACCEPTABLE);
	            }else if(isBlankOrNull(addonStockDTO.getShopId())) {
	            	throw new BusinessException("MISSING PARAM: shopId ", "shopId is required to create addon stock", NOT_ACCEPTABLE);
	            }
	            
	        }else if(Objects.equals(operation, "update")) {
				 if (addonStockDTO.getId() == null) {
					 throw new BusinessException("MISSING PARAM: id", "id is required to update addon stock", NOT_ACCEPTABLE);
				 }  
				
	        }else {
	            throw new BusinessException("INVALID PARAM: operation", "unsupported operation" + addonStockDTO.getOperation(), NOT_ACCEPTABLE);
	        }
		}

		@Override
		public void deleteAddonStock(Long id, Long shopId, Long addonId) throws BusinessException {

			AddonStocksEntity entity = addonStockRepo.findByIdAndShopsEntity_IdAndAddonEntity_Id(id, shopId, addonId)
					.orElseThrow(() -> new BusinessException("INVALID PARAM: id",
							"No addon stock available with provided id", NOT_ACCEPTABLE));
			if (!cartItemAddonDetailsRepository.existsByAddonStockEntity_Id(entity.getId())) {

				addonStockRepo.delete(entity);
			} else {
				throw new BusinessException("", "the stock has items in cart ", NOT_ACCEPTABLE);
			}
		}
	    
	    @Override
			public List<AddonStocksDTO> getAllAddonStocks(Long shopId) {
	    	return addonStockRepo.listStocks(shopId);
	    }
	    
	    @Override
			public void deleteAddonFromProduct(Long addonItemId) throws BusinessException {
	    	CartItemAddonDetailsEntity entity = cartItemAddonDetailsRepository
					.findById(addonItemId)
					.orElseThrow(() -> new BusinessException(
											"INVALID PARAM: id"
											, "No addon item available with provided id"
											, NOT_ACCEPTABLE));
	    	cartItemAddonDetailsRepository.delete(entity);;
	    }
	    
	   @Override
		public  List<AddonDetailsDTO> listItemAddons(Long itemId){
	    	return cartItemAddonDetailsRepository.listItemAddons(itemId);
	    }
	   
	   @Override
		public List<ProductAddonsDTO> getProductAddonsInStock(Long productId,Long shopeId) throws BusinessException{
		   ProductEntity pr=productRepository.getById(productId);
		   Set<AddonEntity> addons=pr.getAddons();
		 
		   List<ProductAddonsDTO> list=new ArrayList<>();
		   for(AddonEntity addon:addons) {
			   AddonStocksEntity st=addonStockRepo.findByShopsEntity_IdAndAddonEntity_Id(shopeId,addon.getId());
					
					   
					   if(st!=null && st.getQuantity()>0) {
					  
             
			   ProductAddonsDTO dto=new ProductAddonsDTO();
			   dto.setAddonId(addon.getId());
			   dto.setName(addon.getName());
			   dto.setType(addon.getType());
			  dto.setQuantity(st.getQuantity());
			  dto.setProductId(productId);
			  list.add(dto);
					   }
		   }
		   
	    	return list;
	    }
	 
}

