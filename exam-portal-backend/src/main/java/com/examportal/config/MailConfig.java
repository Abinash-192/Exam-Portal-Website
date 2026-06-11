package com.examportal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

      @Value("${spring.mail.host}")
      private String host;

      @Value("${spring.mail.port}")
      private int port;

      @Value("${spring.mail.username}")
      private String username;

      @Value("${string.mail.password}")
      private String password;

      @Value("${spring.mail.properties.mail.smtp.auth:true}")
      private String smtpAuth;

      @Value("${spring.mail.properties.mail.smtp.starttles.enable:true}")
      private String starttls;

      @Bean
      public JavaMailSender javaMailSender(){
          JavaMailSenderImpl sender = new JavaMailSenderImpl();
          sender.setHost(host);
          sender.setPort(port);
          sender.setUsername(username);
          sender.setPassword(password);
          sender.setDefaultEncoding("UTF-8");

          Properties props = sender.getJavaMailProperties();
          props.put("mail.transport.protocol","smtp");
          props.put("mail.smtp.auth",smtpAuth);
          props.put("mail.smtp.starttls.enable",starttls);
          props.put("mail.smtp.connectiontimeout","5000");
          props.put("mail.smtp.timeout","5000");
          props.put("mail.smtp.writetimeout","5000");
          props.put("mail.debug","true");

          return  sender;
      }
}
