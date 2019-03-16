package com.nasnav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class NavBox {
    public static void main(String[] args) {
        SpringApplication.run(NavBox.class, args);
    }
}