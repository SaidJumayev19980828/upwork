package com.nasnav.test.external.univocity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RowProcessorErrorHandler;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

public class UniVocityCvsParserTest {
	private final String CSV_WITH_MISSING_VAL = "src/test/resources/files/missing_val.csv";
	private final String CSV_WITH_MISSING_COL = "src/test/resources/files/missing_col.csv";
	private final String CSV_NORM_FILE = "src/test/resources/files/valid.csv";
	private final String CSV_WITH_INVALID_VAL = "src/test/resources/files/invalid_val.csv";  
	private final String CSV_WITH_DUPLICATE_COL = "src/test/resources/files/duplicate_col.csv";
	
	
	
	@Test
	public void parseCvsTest() throws IllegalStateException, FileNotFoundException {
		String filePath = CSV_NORM_FILE;
		ColumnMapping mapper = createAttrToColMapping();
		
		BeanListProcessor<CSVRowBean> rowProcessor = new BeanListProcessor<CSVRowBean>(CSVRowBean.class);
		rowProcessor.setColumnMapper(mapper);
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);
		
		CsvParser parser = new CsvParser(settings);
		parser.parse(new File(filePath));
		parser.getContext().parsedHeaders();
		
		List<CSVRowBean> rows = rowProcessor.getBeans();
        
        assertEquals(2, rows.size());
        assertNotNull(rows.get(0).getQuantity());
	}
	
	
	
	
	
	
	@Test
	public void parseCvsChildBeanTest() throws IllegalStateException, FileNotFoundException {
		String filePath = CSV_NORM_FILE;
		ColumnMapping mapper = createAttrToColMapping();
		
		BeanListProcessor<CSVRowBeanChild> rowProcessor = new BeanListProcessor<CSVRowBeanChild>(CSVRowBeanChild.class);
		rowProcessor.setColumnMapper(mapper);
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);
		
		CsvParser parser = new CsvParser(settings);
		parser.parse(new File(filePath));
		parser.getContext().parsedHeaders();
		
		List<CSVRowBeanChild> rows = rowProcessor.getBeans();
        
        assertEquals(2, rows.size());
        assertNotNull(rows.get(0).getQuantity());        
	}
	
	
	
	
	
	
	@Test
	public void parseCvsMissingValsTest() throws IllegalStateException, FileNotFoundException {
		String filePath = CSV_WITH_MISSING_VAL;
		ColumnMapping mapper = createAttrToColMapping();
		
		BeanListProcessor<CSVRowBean> rowProcessor = new BeanListProcessor<CSVRowBean>(CSVRowBean.class);
		rowProcessor.setColumnMapper(mapper);
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);
		
		CsvParser parser = new CsvParser(settings);
		parser.parse(new File(filePath));
		String[] headers = parser.getContext().parsedHeaders();
		
		List<CSVRowBean> rows = rowProcessor.getBeans();
        
		assertEquals(3 , headers.length);
        assertEquals(2, rows.size());
        assertNull("non existing values are ignored and set as null",rows.get(0).getQuantity());
        assertNotNull(rows.get(1).getQuantity());        
	}
	
	
	
	
	
	@Test(expected = DataProcessingException.class)
	public void parseCvsMissingColsTest() throws IllegalStateException, FileNotFoundException {
		String filePath = CSV_WITH_MISSING_COL;
		ColumnMapping mapper = createAttrToColMapping();
		
		BeanListProcessor<CSVRowBean> rowProcessor = new BeanListProcessor<CSVRowBean>(CSVRowBean.class);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);
		
		CsvParser parser = new CsvParser(settings);
		parser.parse(new File(filePath));		
	}
	
	
	
	
	
	@Test
	public void parseCvsDuplicateColTest() throws IllegalStateException, FileNotFoundException {
		String filePath = CSV_WITH_DUPLICATE_COL;
		ColumnMapping mapper = createAttrToColMapping();
		
		BeanListProcessor<CSVRowBean> rowProcessor = new BeanListProcessor<CSVRowBean>(CSVRowBean.class);
		rowProcessor.setColumnMapper(mapper);
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);
		
		CsvParser parser = new CsvParser(settings);
		parser.parse(new File(filePath));
		String[] headers = parser.getContext().parsedHeaders();
		
		List<CSVRowBean> rows = rowProcessor.getBeans();
        
		assertEquals(4 , headers.length);
        assertEquals(2, rows.size());
        assertNotNull(rows.get(1).getQuantity());        
	}






	private ColumnMapping createAttrToColMapping() {
		ColumnMapping mapper = new ColumnMapping();
		mapper.attributeToColumnName("quantity", "quant");
		mapper.attributeToColumnName("name", "name");
		mapper.attributeToColumnName("price", "price");
		return mapper;
	}
	
	
	
	
	
	
	@Test
	public void csvWithInvalidValTest() {
		List<String> errors = new ArrayList<>();
		ColumnMapping mapper = createAttrToColMapping();
		
		BeanListProcessor<CSVRowBean> rowProcessor = new BeanListProcessor<CSVRowBean>(CSVRowBean.class);
		rowProcessor.setColumnMapper(mapper);
		
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);
		settings.setAutoClosingEnabled(true);
		settings.setProcessorErrorHandler(new RowProcessorErrorHandler() {
			@Override
			public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
					StringBuilder err = new StringBuilder();
					String line1 = String.format("Error processing row[%d]: %s", context.currentLine(), Arrays.toString(inputRow));
					String line2 = String.format("Error details: column '%s' (index %d) has value '%s'", error.getColumnName(), error.getColumnIndex(), inputRow[error.getColumnIndex()]);
					errors.add( err.append(line1).append("\n").append(line2).toString());	
				}
			});

		
		CsvParser parser = new CsvParser(settings);
		parser.parse(new File(CSV_WITH_INVALID_VAL));
		parser.getContext().parsedHeaders();
		
		List<CSVRowBean> rows = rowProcessor.getBeans();
        
        assertEquals(1, rows.size());
        assertEquals(1, errors.size());
        assertNotNull(rows.get(0).getQuantity());

	}
}
