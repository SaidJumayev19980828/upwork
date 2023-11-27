package com.nasnav.yeshtery.test;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.service.OrganizationProcessService;
import com.nasnav.yeshtery.controller.v1.DataImportAsyncController;
import com.nasnav.yeshtery.test.templates.AbstractTestWithTempBaseDir;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase=BEFORE_TEST_METHOD,  scripts={"/sql/Data_Import_API_Test_Data_Insert.sql"})
@Sql(executionPhase=AFTER_TEST_METHOD, scripts= {"/sql/database_cleanup.sql"})
public class AsyncDataImportXlsxApiTest extends AbstractTestWithTempBaseDir {

		@Test
		public void test_importProductListXLSX_success() throws Exception {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			ImportProcessStatusResponse expectedResponse = new ImportProcessStatusResponse();
			when(organizationProcessService.importExcelProductList(any(MultipartFile.class), any(ProductListImportDTO.class))).thenReturn(expectedResponse);
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			ImportProcessStatusResponse actualResponse = controller.importProductListXLSX("token", mock(MultipartFile.class), mock(ProductListImportDTO.class));
			assertEquals(expectedResponse, actualResponse);
		}
		@Test
		public void test_importProductListCSV_success() throws Exception {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			ImportProcessStatusResponse expectedResponse = new ImportProcessStatusResponse();
			when(organizationProcessService.importCsvProductList(any(MultipartFile.class), any(ProductListImportDTO.class))).thenReturn(expectedResponse);
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			ImportProcessStatusResponse actualResponse = controller.importProductListCSV("token", mock(MultipartFile.class), mock(ProductListImportDTO.class));
			assertEquals(expectedResponse, actualResponse);
		}

		@Test
		public void test_getAllProcess_success() {
			// Mock dependencies
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			List<ImportProcessStatusResponse> expectedResponse = new ArrayList<>();
			when(organizationProcessService.getProcessesStatus()).thenReturn(expectedResponse);
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			List<ImportProcessStatusResponse> actualResponse = controller.getAllProcess("token");
			assertEquals(expectedResponse, actualResponse);
		}

		@Test
		public void test_importProductList_invalidMetadata() throws Exception {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			when(organizationProcessService.importExcelProductList(any(MultipartFile.class), any(ProductListImportDTO.class))).thenThrow(new Exception());
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			try {
				controller.importProductListXLSX("token", mock(MultipartFile.class), mock(ProductListImportDTO.class));
				fail("Expected exception was not thrown");
			} catch (Exception e) {
				assertTrue(e instanceof Exception);
			}
		}

		@Test
		public void test_importProductList_invalidFileFormat() throws Exception {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			when(organizationProcessService.importExcelProductList(any(MultipartFile.class), any(ProductListImportDTO.class))).thenThrow(new Exception());
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			try {
				controller.importProductListXLSX("token", mock(MultipartFile.class), mock(ProductListImportDTO.class));
				fail("Expected exception was not thrown");
			} catch (Exception e) {
				assertTrue(e instanceof Exception);
			}
		}
		@Test
		public void test_getProcessStatus_invalidId() {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			when(organizationProcessService.getProcessStatus(anyString())).thenReturn(null);
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			ImportProcessStatusResponse actualResponse = controller.getProcessStatus("token", "invalidId");
			assertNull(actualResponse);
		}

		@Test
		public void test_retrieveProcessStatus_success() {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			ImportProcessStatusResponse expectedResponse = new ImportProcessStatusResponse();
			when(organizationProcessService.getProcessStatus(anyString())).thenReturn(expectedResponse);
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			ImportProcessStatusResponse actualResponse = controller.getProcessStatus("token", "processId");
			assertEquals(expectedResponse, actualResponse);
		}

		@Test
		public void test_retrieveProcessResult_success() {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			Object expectedResponse = new Object();
			when(organizationProcessService.getProcessResult(anyString())).thenReturn(expectedResponse);
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			Object actualResponse = controller.getProcessResult("token", "processId");
			assertEquals(expectedResponse, actualResponse);
		}

		@Test
		public void test_cancelProcess_success() {
			OrganizationProcessService organizationProcessService = mock(OrganizationProcessService.class);
			ImportProcessStatusResponse expectedResponse = new ImportProcessStatusResponse();
			when(organizationProcessService.cancelProcess(anyString())).thenReturn(expectedResponse);
			DataImportAsyncController controller = new DataImportAsyncController(organizationProcessService);
			ImportProcessStatusResponse actualResponse = controller.cancelProcess("token", "processId");
			assertEquals(expectedResponse, actualResponse);
		}




}