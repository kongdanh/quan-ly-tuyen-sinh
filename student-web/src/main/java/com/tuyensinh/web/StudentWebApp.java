package com.tuyensinh.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.tuyensinh")
@EntityScan(basePackages = "com.tuyensinh.model")
public class StudentWebApp {
    public static void main(String[] args) {
        SpringApplication.run(StudentWebApp.class, args);
    }
}