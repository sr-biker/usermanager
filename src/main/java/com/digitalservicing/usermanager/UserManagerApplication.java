package com.digitalservicing.usermanager;

import com.mashape.unirest.http.Unirest;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class UserManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserManagerApplication.class, args);
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

	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
