package com.project.backend.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpUtil {
    public String generateOtp(){
        Random random = new Random();
        int randomNumber = random.nextInt(999999);
        StringBuilder sb = new StringBuilder(Integer.toString(randomNumber));
        while(sb.length()>6){
            sb.append("0");
        }
        while (sb.length()!=6){
            sb.append("0");
        }
        return sb.toString();
    }
}
