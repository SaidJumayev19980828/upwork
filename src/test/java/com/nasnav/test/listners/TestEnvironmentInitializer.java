package com.nasnav.test.listners;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.jdbc.Sql;

/**
 * A Junit test listner that clears the  testing data before running every test.
 * We do this to ensure a deterministic results for running the tests, as some tests doesn't clean
 * up the data it used before, and any existing data can make conflicts with the test data and cause
 * unexpected results. 
 * starts by maven before running the test phase.
 * */


public class TestEnvironmentInitializer extends RunListener {
	
	private static final String TABLE_STATS_QUERY = "SELECT relname as table_name,n_live_tup row_count FROM pg_stat_user_tables ORDER BY n_live_tup DESC";
	private final String PROPERTIES_FILE_PATH = "database.properties";
	private final String CLEAN_DB_SCRIPT = "/sql/database_cleanup.sql";

	private Logger logger = Logger.getLogger(getClass());
     
	 @Override	 
	 public void testRunStarted(Description description) throws Exception {
		 logger.info(">>> Intializing Test Environment ...");
	       
		 clearDB();
	  }
	
	
	 @Override
	public void testFinished(Description description) throws Exception {		
		super.testFinished(description);
		logger.info(">>> Cleaning Test Environment ...");
	       
		clearDB();
	}
	 
	 
	 
	 @Override	 
	 public void testRunFinished(Result result) throws Exception {
		 logger.info(">>> Cleaning Test Environment ...");
	       
		 clearDB();
		 
		 checkTestDataLeakage();
	  }


	 
	 private void checkTestDataLeakage() {
		 logger.info(">>> Check if the Database still has data ...");
		 try {
			 checkAnyTableHasData();
		 }catch(Exception e) {
			 logger.error(">>> Failed to Check if the Database still has data ..." ,e);
		 }
	     
	     logger.info(">>> Check finish ...");
	 }

	 
	 
	 


	private void checkAnyTableHasData() {
		Properties props = getConnectionProps();
		 
		String url = props.getProperty("db.uri");
		String username = props.getProperty("db.user");
		String password = props.getProperty("db.password");
		 
		 
		//jdbi is a library for simplifying running sql
		Jdbi jdbi = Jdbi.create(url, username, password);
		Optional<TableRowCount> tableCnt = 
				jdbi.withHandle(
					handle -> handle.createQuery(TABLE_STATS_QUERY)
									.mapToBean(TableRowCount.class)
									.stream()
									.filter(tc -> !tc.getRowCount().equals(0L) )
									.findFirst()
							);
		if(tableCnt.isPresent()) {
			TableRowCount cnt = tableCnt.get();
			throw new IllegalStateException(
					String.format("TEST DATA LEAKAGE! table [%s] still has [%d] rows!", cnt.getTableName(), cnt.getRowCount()));
		}
	}
	
	


	private void clearDB() {
		logger.info(">>> Clear Test Database start ...");
		 try {
			 cleanTestingData();
		 }catch(Exception e) {
			 logger.error(">>> Failed to Clear Test Database with error ..." ,e);
		 }
	     
	     logger.info(">>> Clear Test Database finish ...");
	}
	
	
	
	
	private void cleanTestingData() {
		 Properties props = getConnectionProps();
		 
		 String url = props.getProperty("db.uri");
		 String username = props.getProperty("db.user");
		 String password = props.getProperty("db.password");
		 
		 
		 String sql = readSqlFile(CLEAN_DB_SCRIPT);
		 
		 //jdbi is a library for simplifying running sql
		 Jdbi jdbi = Jdbi.create(url, username, password);
		 jdbi.withHandle(handle -> handle.execute(sql));
	 }
	
	 

	private Properties getConnectionProps() {
		Properties properties = new Properties();
			
		try (InputStream in = new ClassPathResource(PROPERTIES_FILE_PATH).getInputStream()) {
			if(in == null) {
				String msg = ">>> Failed to read database properties file at [" + PROPERTIES_FILE_PATH + "] ...";
				throw new IllegalStateException(msg);
			}
			
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return properties;
	}
	
	
	
	
	private String readSqlFile(String classpath) {
		String sql = "";
		
		try (InputStream in = new ClassPathResource(classpath).getInputStream()) {
			if(in == null) {
				String msg = ">>> Failed to read clear database sql script file at [" + classpath + "] ...";
				throw new IllegalStateException(msg);
			}
			
			sql = IOUtils.toString(in, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sql;
	}
	
}

