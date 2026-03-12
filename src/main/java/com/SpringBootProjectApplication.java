package com;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class SpringBootProjectApplication {
    public static void main(String[] args) {
        log.info("收到应用启动请求，准备初始化学生实习与实践教学管理系统");
        SpringApplication.run(SpringBootProjectApplication.class, args);
    }

}
