package com.liule.le_ai_agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class LeAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeAiAgentApplication.class, args);
    }

}
