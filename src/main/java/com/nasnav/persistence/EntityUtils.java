package com.nasnav.persistence;

/**
 * Hold all common methods related to entity classes
 */
public class EntityUtils {


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
}
