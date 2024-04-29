package com.project.backend.utils;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {
    Logger logger = LoggerFactory.getLogger(EmailUtil.class);
    @Autowired
    private JavaMailSender javaMailSender;
    public void sendOtpEmail(String email,String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify your email");
        message.setText("Hello Your Otp is " + otp);
        try{
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setTo(email);
            messageHelper.setSubject("Verify your email");
            messageHelper.setText("""
                    <div>
                        <p>Your Otp is %s </p>
                        <p> This Otp is only valid for 4 minutes </p>
                    </div>
                    """.formatted(otp),true);
            javaMailSender.send(mimeMessage);
        }catch(Exception e){
            logger.info("Failed to create",e);
        }
    }
}

