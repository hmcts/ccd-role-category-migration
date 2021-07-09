package com.example.demo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApplicationParams {

    @Value("${idam.client.id}")
    private String idamUsername;

    @Value("${idam.client.secret}")
    private String idamPassword;

}