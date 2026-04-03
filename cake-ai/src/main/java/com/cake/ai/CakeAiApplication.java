package com.cake.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cake.ai.mapper")
@SpringBootApplication
public class CakeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CakeAiApplication.class, args);
    }

}
