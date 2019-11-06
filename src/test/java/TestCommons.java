import org.apache.http.entity.ContentType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public class TestCommons {


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
}
