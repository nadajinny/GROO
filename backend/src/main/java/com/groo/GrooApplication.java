package com.groo;

import com.groo.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class GrooApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrooApplication.class, args);
    }
}
