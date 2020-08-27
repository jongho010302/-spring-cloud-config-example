package net.yogurt.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class YogurtConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(YogurtConfigApplication.class, args);
    }
}
