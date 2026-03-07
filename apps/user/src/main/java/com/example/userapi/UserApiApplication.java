package com.example.userapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Map;

@SpringBootApplication(scanBasePackages = "com.example")
@AutoConfigurationPackage(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example")
public class UserApiApplication {
    public static void main(final String[] args) {
        final SpringApplication app = new SpringApplication(UserApiApplication.class);
        app.setDefaultProperties(Map.of("app.type", "user"));
        app.run(args);
    }
}
