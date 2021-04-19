package com.nasnav.dao;

import com.nasnav.dto.VariantWithNoImagesDTO;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.nasnav.commons.utils.SpringUtils.readOptionalResource;
import static java.util.Collections.emptyList;

@Repository
@Log
public class ProductImgsCustomRepository {
	
	@Value("classpath:/sql/get_products_with_no_imgs.sql")
	private Resource productWithNoImgsSqlFile;
	
	
	@Autowired
	private JdbcTemplate jdbc;
	
	
	
	/**
	 * @return for each product with no images -neither for the product itself or any of its variants-, return one of its variants
	 * with the minimum id.
	 * */
	public List<VariantWithNoImagesDTO> getProductsWithNoImages(Long orgId){
			return readOptionalResource(productWithNoImgsSqlFile)
					.map(sql -> jdbc.query(sql,  new BeanPropertyRowMapper<>(VariantWithNoImagesDTO.class), orgId, orgId, orgId, orgId))
					.orElse(emptyList());
	}
}
