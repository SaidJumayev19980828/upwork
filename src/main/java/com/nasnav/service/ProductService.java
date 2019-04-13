package com.nasnav.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.StocksEntity;

@Service
public class ProductService {

	@Value("${products.default.start}")
	private Integer defaultStart;
	@Value("${products.default.count}")
	private Integer defaultCount;
	@Value("${products.default.sort.attribute}")
	private String defaultSortAttribute;
	@Value("${products.default.order}")
	private String defaultOrder;

	private final ProductRepository productRepository;

	private final StockRepository stockRepository;

	@Autowired
	public ProductService(ProductRepository productRepository, StockRepository stockRepository) {
		this.productRepository = productRepository;
		this.stockRepository = stockRepository;
	}

	public ProductsResponse getProductsResponseByShopId(Long shopId, Long categoryId, Integer start, Integer count,
			String sort, String order) {

		if (start == null)
			start = defaultStart;
		if (count == null)
			count = defaultCount;

		if (sort == null)
			sort = defaultSortAttribute;

		if (order == null)
			order = defaultOrder;

		List<StocksEntity> stocks = stockRepository.findByShopsEntity_Id(shopId);
		List<ProductEntity> products = null;

		if (stocks != null && !stocks.isEmpty()) {

			List<Long> productsIds = stocks.stream().filter(stock -> stock.getProductEntity() != null)
					.map(stock -> stock.getProductEntity().getId()).collect(Collectors.toList());

			if (categoryId == null) {
				products = getProductsByIds(productsIds, order, sort);
			} else {
				products = getProductsByIdsAndCategoryId(productsIds, categoryId, order, sort);
			}
		}
		return getProductsResponse(products, order, sort, start, count);

	}

	public ProductsResponse getProductsResponseByOrganizationId(Long organizationId, Long categoryId, Integer start,
			Integer count, String sort, String order) {
		if (start == null)
			start = defaultStart;
		if (count == null)
			count = defaultCount;

		if (sort == null)
			sort = defaultSortAttribute;

		if (order == null)
			order = defaultOrder;

		List<ProductEntity> products = null;
		if (categoryId == null) {
			products = getProductsForOrganizationId(organizationId, order, sort);
		} else {
			products = getProductsForOrganizationIdAndCategoryId(organizationId, categoryId, order, sort);
		}

		return getProductsResponse(products, order, sort, start, count);
	}

	private List<ProductEntity> getProductsForOrganizationId(Long organizationId, String order, String sort) {

		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdOrderByIdAsc(organizationId);
			} else {
				products = productRepository.findByOrganizationIdOrderByIdDesc(organizationId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {

			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdOrderByNameAsc(organizationId);
			} else {
				products = productRepository.findByOrganizationIdOrderByNameDesc(organizationId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdOrderByPnameAsc(organizationId);
			} else {
				products = productRepository.findByOrganizationIdOrderByPnameDesc(organizationId);
			}
		} else {
			products = productRepository.findByOrganizationId(organizationId);
		}
		return products;
	}

	private List<ProductEntity> getProductsForOrganizationIdAndCategoryId(Long organizationId, Long categoryId,
			String order, String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByIdAsc(organizationId, categoryId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByIdDesc(organizationId, categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {

			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByNameAsc(organizationId,
						categoryId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByNameDesc(organizationId,
						categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByPnameAsc(organizationId,
						categoryId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByPnameDesc(organizationId,
						categoryId);
			}
		} else {
			products = productRepository.findByOrganizationIdAndCategoryId(organizationId, categoryId);
		}
		return products;
	}

	private List<ProductEntity> getProductsByIds(List<Long> productsIds, String order, String sort) {

		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInOrderByIdAsc(productsIds);
			} else {
				products = productRepository.findByIdInOrderByIdDesc(productsIds);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInOrderByNameAsc(productsIds);
			} else {
				products = productRepository.findByIdInOrderByNameDesc(productsIds);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInOrderByPnameAsc(productsIds);
			} else {
				products = productRepository.findByIdInOrderByPnameDesc(productsIds);
			}
		} else {
			products = productRepository.findByIdIn(productsIds);
		}
		return products;
	}

	private List<ProductEntity> getProductsByIdsAndCategoryId(List<Long> productsIds, Long categoryId, String order,
			String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdOrderByIdAsc(productsIds, categoryId);
			} else {
				products = productRepository.findByIdInAndCategoryIdOrderByIdDesc(productsIds, categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdOrderByNameAsc(productsIds, categoryId);
			} else {
				products = productRepository.findByIdInAndCategoryIdOrderByNameDesc(productsIds, categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdOrderByPnameAsc(productsIds, categoryId);
			} else {
				products = productRepository.findByIdInAndCategoryIdOrderByPnameDesc(productsIds, categoryId);
			}
		} else {
			products = productRepository.findByIdInAndCategoryId(productsIds, categoryId);
		}
		return products;
	}

	private ProductsResponse getProductsResponse(List<ProductEntity> products, String order, String sort, Integer start,
			Integer count) {

		ProductsResponse productsResponse = null;
		if (products != null) {
			productsResponse = new ProductsResponse();

			List<StocksEntity> stocks = stockRepository.findByProductEntity_IdIn(
					products.stream().map(product -> product.getId()).collect(Collectors.toList()));

			List<ProductRepresentationObject> productsRep = products.stream()
					.map(product -> (ProductRepresentationObject) product.getRepresentation())
					.collect(Collectors.toList());
			productsRep.forEach(pRep -> {

				Optional<StocksEntity> optionalStock = stocks.stream().filter(stock -> stock != null
						&& stock.getProductEntity() != null && stock.getProductEntity().getId().equals(pRep.getId()))
						.findFirst();

				if (optionalStock != null && optionalStock.isPresent()) {
					pRep.setAvailable(true);
					pRep.setPrice(optionalStock.get().getPrice());
				} else {
					pRep.setAvailable(false);
				}

			});

			if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.PRICE) {

				if (order.equals("desc")) {

					Collections.sort(productsRep, new Comparator<ProductRepresentationObject>() {
						@Override
						public int compare(ProductRepresentationObject p1, ProductRepresentationObject p2) {
							if (p1.getPrice() == null)
								return -1;
							if (p2.getPrice() == null)
								return 1;
							return Double.compare(p1.getPrice().doubleValue(), p2.getPrice().doubleValue());
						}
					});

				} else {

					Collections.sort(productsRep, new Comparator<ProductRepresentationObject>() {
						@Override
						public int compare(ProductRepresentationObject p1, ProductRepresentationObject p2) {
							if (p1.getPrice() == null)
								return 1;
							if (p2.getPrice() == null)
								return -1;
							return Double.compare(p2.getPrice().doubleValue(), p1.getPrice().doubleValue());
						}
					});
				}
			}
			int lastProductIndex = productsRep.size()-1;
			if(start>lastProductIndex) {
				//TODO proper start handling(maybe default) or error massage
				return null;
			}
			int toIndex = start+count;
			if(toIndex> lastProductIndex)
				toIndex = productsRep.size();
			
			productsResponse.setProducts(productsRep.subList(start, toIndex));
			productsResponse.setTotal(stocks.stream().mapToLong(stock -> stock.getQuantity()).sum());
		}

		return productsResponse;
	}
}
