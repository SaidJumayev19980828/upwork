import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public class TestCommons {

    public static String BaseURL = "";
    public static String TestUserEmail = "nonexistent@nasnav.com";

    private static HttpHeaders authHeaders(long userId, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-ID", Long.toString(userId));
        headers.add("User-Token", authToken);
        return headers;
    }

    public static HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public static HttpEntity<Object> getHttpEntity(String json, long userId, String authToken) {
        HttpHeaders headers = authHeaders(userId, authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }

    public static HttpEntity<Object> getHttpEntity(MultiValueMap<String, String> parameters, long userId, String authToken) {
        HttpHeaders headers = authHeaders(userId, authToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(parameters, headers);
    }
}
