package com.example.rtnt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RtntApplication {
    static void main(String[] args) {
        SpringApplication.run(RtntApplication.class, args);
    }
}
