package com.nasnav.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import org.elasticsearch.common.collect.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.handler.chain.process.ImportDataHandlingChainProcess;
import com.nasnav.service.model.importproduct.context.Error;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

@ExtendWith(MockitoExtension.class)
class ImportDataHandlingChainProcessTest {
	private static final String SOME_ERROR_FOR_TEST = "some error for test";

	@Mock
	Handler<ImportDataCommand> mockedHandler;

	ImportDataHandlingChainProcess chainProcess;
	
	@BeforeEach
	void setup() {
		chainProcess = new ImportDataHandlingChainProcess(ImportDataCommand.builder().build(), List.of(mockedHandler));
	}

	@Test
	void testErrorReportingForImportProductException() throws Exception {
		ImportProductContext context = new ImportProductContext();
		context.getErrors().add(new Error(SOME_ERROR_FOR_TEST, null));
		Mockito.doThrow(new ImportProductException(null, context)).when(mockedHandler).handle(any(), any());

		chainProcess.run();

		assertEquals(SOME_ERROR_FOR_TEST, chainProcess.getResult().getErrors().get(0).getMessage());
	}

	@Test
	void testErrorReportingForOtherExceptions() throws Exception {
		Mockito.doThrow(new Exception(SOME_ERROR_FOR_TEST)).when(mockedHandler).handle(any(), any());

		chainProcess.run();

		assertEquals(SOME_ERROR_FOR_TEST, chainProcess.getResult().getErrors().get(0).getMessage());
	}
}
