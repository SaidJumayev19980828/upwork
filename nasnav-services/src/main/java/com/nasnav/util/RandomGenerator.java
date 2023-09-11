package com.nasnav.util;

import com.mchange.util.AssertException;

import java.security.SecureRandom;
import java.util.Random;

public final class RandomGenerator {

    public static String randomNumber(int len) {
        String numbers = "0123456789";
        Random randomMethod = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            builder.append(numbers.charAt(randomMethod.nextInt(numbers.length())));
        }
        return builder.toString();
    }

    private RandomGenerator() {
        throw new AssertException();
    }
}
