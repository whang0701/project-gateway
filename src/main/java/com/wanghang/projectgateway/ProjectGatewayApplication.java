package com.wanghang.projectgateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients("com.wanghang.*")
@ComponentScan("com.wanghang.*")
@MapperScan("com.wanghang.projectsdk.base.dao")
public class ProjectGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectGatewayApplication.class, args);
    }

}
