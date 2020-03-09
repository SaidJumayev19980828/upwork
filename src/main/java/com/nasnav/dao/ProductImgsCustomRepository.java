package com.nasnav.dao;

import static com.nasnav.commons.utils.SpringUtils.readResource;
import static java.util.Collections.emptyList;
import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nasnav.dto.VariantWithNoImagesDTO;

import lombok.extern.java.Log;

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
		try {
			String sql = readResource(productWithNoImgsSqlFile);
			return jdbc.query(sql,  new BeanPropertyRowMapper<>(VariantWithNoImagesDTO.class), orgId, orgId, orgId, orgId);
		} catch (IOException e) {
			log.log(SEVERE, e.getMessage(), e);
			return emptyList();
		}
	}
}
