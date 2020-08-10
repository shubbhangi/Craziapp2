package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.technohertz.util.FileStorageProperties;

@SpringBootApplication(scanBasePackages = { "com.example.demo" })
@EnableCaching
@EnableScheduling
public class Craziapp2Application {

	public static void main(String[] args) {
		SpringApplication.run(Craziapp2Application.class, args);
	}

}
