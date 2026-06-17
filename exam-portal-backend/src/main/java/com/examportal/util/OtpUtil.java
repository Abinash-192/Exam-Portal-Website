package com.examportal.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpUtil {

    private static  final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 8;

    public String generateNumericOtp(){

        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {

            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
