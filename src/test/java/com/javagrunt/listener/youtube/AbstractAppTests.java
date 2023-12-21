package com.javagrunt.listener.youtube;

import com.redis.testcontainers.RedisContainer;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
@Testcontainers
public abstract class AbstractAppTests {

    static Logger logger = LoggerFactory.getLogger(AbstractAppTests.class);

    abstract int getPort();

    private RequestSpecification spec;
    private static final Network network = Network.newNetwork();
    
    static Network getNetwork(){
        return network;
    }

    @Container
    @ServiceConnection(name = "redis")
    static final RedisContainer redis = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG))
            .withExposedPorts(6379)
            .withNetworkAliases("redis")
            .withNetwork(network);

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
                .port(getPort())
                .get("/api/?hub.mode=subscribe&hub.challenge=CHALLENGE_STRING&hub.topic=somthingcool")
                .then()
                .assertThat().statusCode(is(200))
                .assertThat().body(is("CHALLENGE_STRING"));
    }

    @Test
    void postShouldReturnSuccess() throws Exception {
        String exampleEvent = """
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
        given(this.spec)
                .filter(document("listen",
                        preprocessRequest(modifyUris()
                                .scheme("https")
                                .host("youtube-listener.javagrunt.com")
                                .removePort())))
                .contentType("application/atom+xml")
                .body(exampleEvent)
                .when()
                .port(getPort())
                .post("/api/")
                .then()
                .assertThat()
                .statusCode(is(200));
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
                .port(getPort())
                .get("/actuator/info")
                .then()
                .assertThat().statusCode(is(200));
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
                .port(getPort())
                .get("/actuator/health")
                .then()
                .assertThat().statusCode(is(200));
    }

    @Test
    void redisShouldBeRunning() {
        Assertions.assertTrue(redis.isRunning());
    }
    
    @AfterAll
    static void tearDown() {
        redis.stop();
    }
    
}
