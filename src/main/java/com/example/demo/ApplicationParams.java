package com.example.demo;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;



@Getter
public class ApplicationParams {

    @Value("${idam.client.id}")
    private String idamUsername;

    @Value("${idam.client.secret}")
    private String idamPassword;

}