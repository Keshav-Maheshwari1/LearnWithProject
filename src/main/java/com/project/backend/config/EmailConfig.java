package com.project.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class EmailConfig {
    @Value("${spring.mail.host}")
    private String mailHost;
    @Value("${spring.mail.port}")
    private String mailPort;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.password}")
    private String mailPassword;

    public JavaMailSender getJavaMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setUsername(mailUsername);
        javaMailSender.setPort(Integer.parseInt(mailPort));
        javaMailSender.setHost(mailHost);
        javaMailSender.setPassword(mailPassword);
        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.smtp.starttls.enable","true");
        return javaMailSender;
    }
}
