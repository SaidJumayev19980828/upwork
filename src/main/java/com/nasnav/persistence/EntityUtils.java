package com.nasnav.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.util.*;

/**
 * Hold all common methods related to entity classes
 */
public class EntityUtils {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static Random random ;

    private static Random getRandom(){
        if(random == null){
            random = new Random();
        }
        return random;
    }
    /**
     * match the given input param against the passed pattern
     *
     * @param input to be matched to regex
     * @param regex to be used to match input
     * @return true if input match the given regex.
     */
    public static boolean patternMatcher(String input, String regex) {
        if (input == null) {
            return false;
        }
        return input.matches(regex);
    }

    /**
     * validate the name param against Name_PATTERN
     *
     * @param name to be matched against Name_PATTERN
     * @return true if name match Name_PATTERN
     */
    public static boolean validateName(String name) {
        return patternMatcher(name, EntityConstants.Name_PATTERN);
    }

    /**
     * validate the email param against EMAIL_PATTERN
     *
     * @param email to be matched against EMAIL_PATTERN
     * @return true if email match EMAIL_PATTERN
     */
    public static boolean validateEmail(String email) {
        return patternMatcher(email, EntityConstants.EMAIL_PATTERN);
    }

    /**
     * check if passed object is not null and not empty.
     *
     * @param object object to be checked.
     * @return true if object is not null and not empty.
     */
    public static boolean isNotBlankOrNull(Object object) {
        return !isBlankOrNull(object);
    }

    /**
     * check if passed object is null or empty.
     *
     * @param object object to be checked.
     * @return true if object is empty or null.
     */
    public static boolean isBlankOrNull(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof String) {
            return ((String) object).isEmpty();
        }
        if (object instanceof Collection) {
            return ((Collection) object).isEmpty();
        }
        return false;
    }

    /**
     * Generate new token whose length equal the passed length.
     *
     * @param length length of token to be generated.
     * @return new token string.
     */
    public static String generateToken(int length) {
        StringBuilder builder = new StringBuilder(10);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(getRandom().nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    /**
     * Create failed login api response
     *
     * @param responseStatuses failed response statuses
     * @return ApiResponse
     */
    public static ApiResponse createFailedLoginResponse(List<ResponseStatus> responseStatuses) {
        return new ApiResponseBuilder().setSuccess(false).setResponseStatuses(responseStatuses).build();
    }
}
