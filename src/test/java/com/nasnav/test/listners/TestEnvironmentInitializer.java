package com.nasnav.test.listners;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
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
 * A Junit test listner that clears the whole testing database before running any tests.
 * We do this to ensure a determinitic results for running the tests, as some tests doesn't clean
 * up the data it used before, and any existing data can make conflicts with the test data and cause
 * unexpected results. 
 * starts by maven before running the test phase.
 * */


public class TestEnvironmentInitializer extends RunListener {
	
	private final String PROPERTIES_FILE_PATH = "database.properties";

	private Logger logger = Logger.getLogger(getClass());
     
	 @Override	 
	 public void testRunStarted(Description description) throws Exception {
		 logger.info(">>> Intializing Test Environment ...");
	       
		 clearDB();
	  }
	 
	 
	 
	 @Override	 
	 public void testRunFinished(Result result) throws Exception {
		 logger.info(">>> Cleaning Test Environment ...");
	       
		 clearDB();
	  }





	private void clearDB() {
		logger.info(">>> Clear Test Database start ...");
		 try {
//			 truncateTestDbTables();
		 }catch(Exception e) {
			 logger.error(">>> Failed to Clear Test Database with error ..." ,e);
		 }
	     
	     logger.info(">>> Clear Test Database finish ...");
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

