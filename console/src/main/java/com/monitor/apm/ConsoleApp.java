package com.monitor.apm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.monitor.apm"})
public class ConsoleApp {
    public static void main(String[] args) {
        SpringApplication.run(ConsoleApp.class,args);
    }
}
