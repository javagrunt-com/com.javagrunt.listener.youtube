package com.javagrunt.listener.youtube;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;


import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;


@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebLayerTest {

	private RequestSpecification spec;

	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.spec = new RequestSpecBuilder()
				.addFilter(documentationConfiguration(restDocumentation))
				.build();
	}

	@Test
	public void getShouldEchoHubChallenge() {
		given(this.spec)
				.filter(document("hello",
						preprocessRequest(modifyUris()
								.scheme("https")
								.host("youtube-listener.javagrunt.com")
								.removePort())))
				.when()
				.port(this.port)
				.get("/api/?hub.mode=subscribe&hub.challenge=CHALLENGE_STRING&hub.topic=somthingcool")
				.then()
				.assertThat().statusCode(is(200))
				.assertThat().body(is("CHALLENGE_STRING"));
	}

	@Test
	void postShouldReturnSuccess() throws Exception {
		given(this.spec)
				.filter(document("listen",
						preprocessRequest(modifyUris()
								.scheme("https")
								.host("youtube-listener.javagrunt.com")
								.removePort())))
				.contentType("application/atom+xml")
				.body(exampleEvent)
				.when()
				.port(this.port)
				.post("/api/")
				.then()
				.assertThat()
				.statusCode(is(200));
	}

	@Test
	public void actuatorHealth() {
		given(this.spec)
				.filter(document("health",
						preprocessRequest(modifyUris()
								.scheme("https")
								.host("youtube-listener.javagrunt.com")
								.removePort())))
				.when()
				.port(this.port)
				.get("/actuator/health")
				.then()
				.assertThat().statusCode(is(200));
	}

	@Test
	public void actuatorInfo() {
		given(this.spec)
				.filter(document("info",
						preprocessRequest(modifyUris()
								.scheme("https")
								.host("youtube-listener.javagrunt.com")
								.removePort())))
				.when()
				.port(this.port)
				.get("/actuator/info")
				.then()
				.assertThat().statusCode(is(200));
	}
	
	private String exampleEvent = """
			<feed xmlns:yt="http://www.youtube.com/xml/schemas/2015"
			         xmlns="http://www.w3.org/2005/Atom">
			  <link rel="hub" href="https://pubsubhubbub.appspot.com"/>
			  <link rel="self" href="https://www.youtube.com/xml/feeds/videos.xml?channel_id=CHANNEL_ID"/>
			  <title>YouTube video feed</title>
			  <updated>2015-04-01T19:05:24.552394234+00:00</updated>
			  <entry>
			    <id>yt:video:VIDEO_ID</id>
			    <yt:videoId>VIDEO_ID</yt:videoId>
			    <yt:channelId>CHANNEL_ID</yt:channelId>
			    <title>Video title</title>
			    <link rel="alternate" href="http://www.youtube.com/watch?v=VIDEO_ID"/>
			    <author>
			     <name>Channel title</name>
			     <uri>http://www.youtube.com/channel/CHANNEL_ID</uri>
			    </author>
			    <published>2015-03-06T21:40:57+00:00</published>
			    <updated>2015-03-09T19:05:24.552394234+00:00</updated>
			  </entry>
			</feed>
			""";

}
