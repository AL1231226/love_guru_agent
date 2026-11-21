package com.liule.le_ai_agent.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Actor {
    private String name;
    private int age;
    private String[] movies;
}
