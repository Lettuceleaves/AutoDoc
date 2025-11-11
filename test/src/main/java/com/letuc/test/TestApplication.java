package com.letuc.test;

import com.letuc.app.entry.AutoDocStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        AutoDocStarter.run();
        SpringApplication.run(TestApplication.class, args);
    }
}
