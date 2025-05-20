package com.example.eventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EventserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(EventserviceApplication.class, args);
  }
}
