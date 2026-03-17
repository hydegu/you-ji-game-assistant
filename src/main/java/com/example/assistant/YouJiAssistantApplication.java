package com.example.assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.assistant.mapper")
public class YouJiAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouJiAssistantApplication.class, args);
    }

}
