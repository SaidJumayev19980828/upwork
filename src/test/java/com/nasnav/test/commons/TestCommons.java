package com.nasnav.test.commons;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.Cookie;

import org.jdbi.v3.core.Jdbi;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        HttpCookie cookie = new HttpCookie("User-Token", authToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        //headers.add("User-Token", authToken);
        return headers;
    }

    public static HttpEntity<Object> getHttpEntity(MultiValueMap<?, ?> json, String authToken, MediaType type) {
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
    
    
    
    public static JSONObject json() {
    	return new JSONObject();
    }
    
    
    
    
    public static JSONArray jsonArray() {
    	return new JSONArray();
    }
    
    
    
    
    public static String readResource(Resource resource) throws IOException {
    	return new String( Files.readAllBytes(resource.getFile().toPath()) );
    }
    
    
    
    public static Optional<String> extractAuthTokenFromCookies(ResponseEntity<?> response) {
		return 
			ofNullable(response)
			.map(ResponseEntity::getHeaders)
			.map(headers -> headers.getFirst(SET_COOKIE))						
			.map(TestCommons::readCookie)
			.filter(cookie -> Objects.equals(cookie.get().getName(), TOKEN_HEADER))
			.map(Optional::get)
			.map(Cookie::getValue);
	}


	
	private static Optional<Cookie> readCookie(String cookieStr){
		return ofNullable(cookieStr)
				.map(allCookieStr -> asList(allCookieStr.split(";")))
				.orElse(emptyList())
				.stream()
				.map(cookieField -> cookieField.trim().split("="))
				.filter(parts -> parts.length == 2)
				.map(parts -> new Cookie(parts[0], parts[1]))
				.findFirst();
	}
}
