package com.codingshuttle.razorpay.common.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class RandomizerUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom(); // Thread safe
    public static String randomBase64(int length){
        byte[] buf = new byte[length];
        SECURE_RANDOM.nextBytes(buf); // Fills the array with random values ex: [-12, 55, 101, -8, 44, ...]
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
