package com.nasnav.test;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import lombok.extern.slf4j.Slf4j;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@NotThreadSafe 
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD , scripts = {"/sql/Products_Export_Test_Data.sql"})
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD , scripts = {"/sql/database_cleanup.sql"})
@Slf4j
public class DataExportTest extends AbstractTestWithTempBaseDir {
	
	
	@Autowired
	private TestRestTemplate template;

	@Test
	public void testProductExportCsv(){
		var response =
				template.exchange("/export/products?shop_id=502&type=CSV", GET, getHttpEntity("192021"), String.class);
		
		log.debug(">>>>>>>\n{}", response.getBody());
		
		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportCSV(){
		var response =
				template.exchange("/export/products/csv?shop_id=502", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportCsvShopFromAnotherOrganization(){
		var response =
				template.exchange("/export/products?shop_id=501&type=CSV", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	public void testProductExportCsvForWholeOrganization(){
		var response =
				template.exchange("/export/products?type=CSV", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportCsvNoAuthz(){
		var response =
				template.exchange("/export/products?shop_id=502&type=CSV", GET, getHttpEntity("101112"), String.class);
		
		log.debug(">>>>>>>\n{}", response.getBody());
		
		assertEquals(FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void testProductExportCSVNoAuthz(){
		var response =
				template.exchange("/export/products/csv?shop_id=502", GET, getHttpEntity("101112"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void testProductExportXlsx(){
		var response =
				template.exchange("/export/products?shop_id=502&type=XLSX", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void generateProductsImagesXlsxTest(){
		var response =
				template.exchange("/export/products/images?type=XLSX", GET, getHttpEntity("192021"), String.class);

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void generateProductsImagesCSVTest(){
		var response =
				template.exchange("/export/products/images?type=CSV", GET, getHttpEntity("192021"), String.class);

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportXlsxShopFromAnotherOrganization(){
		var response =
				template.exchange("/export/products?shop_id=501&type=XLSX", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	public void testProductExportXlsxForWholeOrganization(){
		var response =
				template.exchange("/export/products?type=XLSX", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportXlsxNoAuthz(){
		var response =
				template.exchange("/export/products?shop_id=502&type=XLSX", GET, getHttpEntity("101112"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void testProductExportXLSXNoAuthz(){
		var response =
				template.exchange("/export/products/xlsx?shop_id=502", GET, getHttpEntity("101112"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void testProductsExportXLSX(){
		var response =
				template.exchange("/export/products/xlsx?shop_id=502", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportXLSXShopFromAnotherOrganization(){
		var response =
				template.exchange("/export/products/xlsx?shop_id=501", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD , scripts = {"/sql/Products_Export_Test_Data.sql", "/sql/Products_Export_Test_Extra_Data.sql"})
	@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD , scripts = {"/sql/database_cleanup.sql"})
	public void testProductExportXLSXForWholeOrganization(){
		var response =
				template.exchange("/export/products/xlsx", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductsExportCSV(){
		var response =
				template.exchange("/export/products/csv?shop_id=502", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	public void testProductExportCSVShopFromAnotherOrganization(){
		var response =
				template.exchange("/export/products/csv?shop_id=501", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}

	@Test
	public void testProductExportCSVForWholeOrganization(){
		var response =
				template.exchange("/export/products/csv", GET, getHttpEntity("192021"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}


	@Test
	public void testProductExportCSVForWholeOrganizationByStoreManager(){
		var response =
				template.exchange("/export/products/csv", GET, getHttpEntity("TTTRRR"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	@Test
	public void testProductExportCSVForInvalidShopByStoreManager(){
		var response =
				template.exchange("/export/products/csv?shop_id=503", GET, getHttpEntity("TTTRRR"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(NOT_ACCEPTABLE, response.getStatusCode());
	}


	@Test
	public void testProductExportCSVForByStoreManager(){
		var response =
				template.exchange("/export/products/csv?shop_id=502", GET, getHttpEntity("TTTRRR"), String.class);

		log.debug(">>>>>>>\n{}", response.getBody());

		assertEquals(OK, response.getStatusCode());
		assertFalse(response.getBody().isEmpty());
	}
}
