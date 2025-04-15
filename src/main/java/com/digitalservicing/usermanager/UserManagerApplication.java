package com.digitalservicing.usermanager;

import com.mashape.unirest.http.Unirest;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Entry point for the application.
 */
@SpringBootApplication
public class UserManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserManagerApplication.class, args);
		initTimezone();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				shutdownUnirest();
			}
		}));
	}

	private static void shutdownUnirest() {
		try {
			Unirest.shutdown();
		} catch (IOException e) {
			//Ignore
		}
	}

	public static void initTimezone(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
