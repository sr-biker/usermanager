package com.digitalservicing.usermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Entry point for the application.
 */
@SpringBootApplication
public class UserManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserManagerApplication.class, args);
		initTimezone();
	}

	public static void initTimezone(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
