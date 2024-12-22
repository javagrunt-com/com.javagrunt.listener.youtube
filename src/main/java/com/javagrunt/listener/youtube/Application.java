package com.javagrunt.listener.youtube;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import io.lettuce.core.resource.ClientResources;
import io.micrometer.observation.ObservationRegistry;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.connection.lettuce.observability.MicrometerTracingAdapter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

@RestController
@RequestMapping("/api")
class YouTubeController {

	EventRepository eventRepository;

	Logger logger = LoggerFactory.getLogger(YouTubeController.class);

	YouTubeController(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	
	@PostConstruct
	void init(){
		RestClient defaultClient = RestClient.create();
		String result = defaultClient.get()
				.uri("https://www.youtube.com/feeds/videos.xml?channel_id=UCuGoHRQbVXa4LxepmPOdUfQ")
				.retrieve()
				.body(String.class);
		logger.info("Initialized");
		Assert.notNull(result, "Result is null");
		logger.info(listen(result));
	}

	@PostMapping(value = "/", consumes = "application/atom+xml")
	String listen(@RequestBody String atomXml) {
		logger.info("Received: " + atomXml);
		try {
			SyndFeed feed = new SyndFeedInput()
					.build(
							new XmlReader(
									new ByteArrayInputStream(atomXml.getBytes(StandardCharsets.UTF_8))));
			for (SyndEntry entry : feed.getEntries()) {
				String key = "feed:%s".formatted(entry.getUri());
				YouTubeEvent youTubeEvent = new YouTubeEvent(key,atomXml);
				eventRepository.save(youTubeEvent);
				logger.info(entry.toString());
			}
		} catch (IOException e) {
			logger.error("Error parsing atom xml", e);
		} catch (FeedException e) {
			logger.error("Feed Exception", e);
		}
		return "OK";
	}

	@GetMapping(value = "/")
	String hello(@RequestParam("hub.mode") String mode,
			@RequestParam("hub.challenge") String challenge,
			@RequestParam("hub.topic") String topic) {
		logger.info("Received: " + mode + " " + challenge + " " + topic);
		return challenge;
	}
}

@RedisHash
record YouTubeEvent(@Id String id, String entryXml){}


@Configuration
@EnableRedisRepositories
class ApplicationConfig {
	
	@Bean
	public ClientResources clientResources(ObservationRegistry observationRegistry) {
		return ClientResources.builder()
				.tracing(new MicrometerTracingAdapter(observationRegistry, "youtube-listener"))
				.build();
	}
}
interface EventRepository extends CrudRepository<YouTubeEvent, String> {
}