package com.ams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AMSApplication {

    public static void main(String[] args) {
        SpringApplication.run(AMSApplication.class, args);
        System.out.println("=========================================================");
        System.out.println("  Airline Management System (AMS) Backend Microservice  ");
        System.out.println("  Running on port 8080: http://localhost:8080/api        ");
        System.out.println("=========================================================");
    }
}
