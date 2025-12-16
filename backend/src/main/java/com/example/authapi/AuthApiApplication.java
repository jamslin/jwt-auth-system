package com.example.authapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * =====================================================================
 * MAIN APPLICATION CLASS
 * =====================================================================
 * 
 * @SpringBootApplication is a convenience annotation that combines:
 * 
 * 1. @Configuration    - Marks this class as a source of bean definitions
 * 2. @EnableAutoConfiguration - Tells Spring Boot to automatically configure
 *                               beans based on classpath and properties
 * 3. @ComponentScan    - Tells Spring to scan for components in this package
 *                        and sub-packages
 * 
 * This is the entry point of our application.
 */
@SpringBootApplication
public class AuthApiApplication {

    public static void main(String[] args) {
        // SpringApplication.run() bootstraps the application:
        // - Creates ApplicationContext
        // - Registers all beans
        // - Starts embedded Tomcat server
        SpringApplication.run(AuthApiApplication.class, args);
    }
}
