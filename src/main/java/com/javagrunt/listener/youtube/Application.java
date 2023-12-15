package com.javagrunt.listener.youtube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

@RestController
class YouTubeController {

	Logger logger = LoggerFactory.getLogger(YouTubeController.class);

	@PostMapping(value = "/", consumes = "application/atom+xml")
	String listen(@RequestBody String atomXml) {
		logger.info("Received: " + atomXml);
		return "OK";
	}	
}