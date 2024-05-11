package com.nasnav.test;

import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.handler.chain.process.ImportDataHandlingChainProcess;
import com.nasnav.service.model.importproduct.context.Error;
import com.nasnav.service.model.importproduct.context.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ImportDataHandlingChainProcessTest {
	private static final String SOME_ERROR_FOR_TEST = "some error for test";

	@Mock
	Handler<ImportDataCommand> mockedHandler;

	ImportDataHandlingChainProcess chainProcess;
	
	void setup() {
		chainProcess = new ImportDataHandlingChainProcess(ImportDataCommand.builder().build(), List.of(mockedHandler), null);
	}

	void setupWithFinishingCallback()
	{
		chainProcess = new ImportDataHandlingChainProcess(ImportDataCommand.builder().build(), List.of(mockedHandler), (processId) -> {
			System.out.println("Process with id " + processId + " finished");
		});
	}

	@Test
	void testErrorReportingForImportProductException() throws Exception {
		setup();
		ImportProductContext context = new ImportProductContext();
		context.getErrors().add(new Error(SOME_ERROR_FOR_TEST, null));
		Mockito.doThrow(new ImportProductException(null, context)).when(mockedHandler).handle(any(), any());

		chainProcess.run();

		assertEquals(SOME_ERROR_FOR_TEST, chainProcess.getResult().getErrors().get(0).getMessage());
	}

	@Test
	void testErrorReportingForImportProductExceptionWithFinishingCallback() throws Exception
	{
		setupWithFinishingCallback();
		ImportProductContext context = new ImportProductContext();
		context.getErrors().add(new Error(SOME_ERROR_FOR_TEST, null));
		Mockito.doThrow(new ImportProductException(null, context)).when(mockedHandler).handle(any(), any());

		chainProcess.run();

		assertEquals(SOME_ERROR_FOR_TEST, chainProcess.getResult().getErrors().get(0).getMessage());
	}

	@Test
	void testErrorReportingForOtherExceptions() throws Exception {
		setup();
		Mockito.doThrow(new Exception(SOME_ERROR_FOR_TEST)).when(mockedHandler).handle(any(), any());

		chainProcess.run();

		assertEquals(SOME_ERROR_FOR_TEST, chainProcess.getResult().getErrors().get(0).getMessage());
	}
}
