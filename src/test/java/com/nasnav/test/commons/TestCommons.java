package com.nasnav.test.commons;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jdbi.v3.core.Jdbi;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public class TestCommons {
	private static final String PROPERTIES_FILE_PATH = "database.properties";
	

    public static String BaseURL = "";
    public static String TestUserEmail = "nonexistent@nasnav.com";
    public static long orgId = 99001;

    public static HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public static HttpEntity<Object> getHttpEntity(String json, String authToken) {
        HttpHeaders headers = getHeaders(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }

    public static HttpEntity<Object> getHttpEntity(String authToken) {
        HttpHeaders headers = getHeaders(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    public static HttpEntity<Object> getHttpEntity(MultiValueMap<String, String> parameters, String authToken) {
        HttpHeaders headers = getHeaders(authToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(parameters, headers);
    }

    public static HttpHeaders getHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", authToken);
        return headers;
    }

    public static HttpEntity<Object> getHttpEntity(MultiValueMap json, String authToken, MediaType type) {
        HttpHeaders headers = getHeaders(authToken);
        headers.setContentType(type);
        return new HttpEntity<>(json, headers);
    }
    
    
    
    
    
    /**
     * jdbi is a library for simplifying running sql
     * */
    public static Jdbi getJdbi() {
    	Properties props = getConnectionProps();
    	
    	String url = props.getProperty("db.uri");
		String username = props.getProperty("db.user");
		String password = props.getProperty("db.password");
		 
		return Jdbi.create(url, username, password);
    } 
    
    
    
    
    
    private static Properties getConnectionProps() {
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
}
