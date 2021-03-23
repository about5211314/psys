package com.cn.psys;


import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.cn.psys.system.mapper.*")
public class PsysApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PsysApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(PsysApplication.class, args);
    }

}
