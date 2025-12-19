package com.celalabaci.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling; // YENİ IMPORT

@ComponentScan(basePackages ={"com.celalabaci"})
@EntityScan(basePackages = {"com.celalabaci"})
@EnableJpaRepositories(basePackages = {"com.celalabaci"})
@EnableScheduling // ZAMANLANMIŞ GÖREVLER İÇİN EKLENDİ
@SpringBootApplication
public class SuperVpnProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuperVpnProjectApplication.class, args);
    }

}
