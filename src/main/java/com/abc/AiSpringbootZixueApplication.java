package com.abc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.abc.mapper") // 写你的mapper包路径！！
public class AiSpringbootZixueApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSpringbootZixueApplication.class, args);
    }

}
