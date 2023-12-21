package com.javagrunt.listener.youtube;

import fr.brouillard.oss.jgitver.GitVersionCalculator;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.LazyFuture;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Future;

public class NativeImageIT extends AbstractAppTests {

    private static final Future<String> IMAGE_FUTURE = new LazyFuture<>() {
        @Override
        protected String resolve() {
            // Find project's root dir
            File cwd;
            cwd = new File(".");
            while (!new File(cwd, "mvnw").isFile()) {
                cwd = cwd.getParentFile();
            }

            var request = new DefaultInvocationRequest()
                    .addShellEnvironment("DOCKER_HOST", DockerClientFactory.instance().getTransportConfig().getDockerHost().toString())
                    .setPomFile(new File(cwd, "pom.xml"))
                    .setGoals(List.of("spring-boot:build-image"))
                    .setMavenExecutable(new File(cwd, "mvnw"))
                    .setProfiles(List.of("native"));

            InvocationResult invocationResult;
            try {
                invocationResult = new DefaultInvoker().execute(request);
            } catch (MavenInvocationException e) {
                throw new RuntimeException(e);
            }

            if (invocationResult.getExitCode() != 0) {
                throw new RuntimeException(invocationResult.getExecutionException());
            }

            String semanticVersion = null;
            File workDir = new File(System.getProperty("user.dir"));

            try (GitVersionCalculator jgitver = GitVersionCalculator.location(workDir)) {
                semanticVersion = jgitver.getVersion().split("-")[0];
            } catch (Exception e) {
                logger.error("Error getting semantic version", e);
            }

            if (System.getProperty("os.arch").contains("aarch")) {
                return String.format("dashaun/com.javagrunt.listener.youtube:v%s-aarch_64", semanticVersion);
            } else {
                return String.format("dashaun/com.javagrunt.listener.youtube:v%s-amd_64", semanticVersion);
            }
        }
    };

    private static int port;

    @Override
    int getPort() {
        return port;
    }


    @Container
    static final GenericContainer<?> APP = new GenericContainer<>(IMAGE_FUTURE)
            .withExposedPorts(8080)
            .withNetworkAliases("app")
            .withNetwork(getNetwork())
            .withEnv("REDIS_PORT", "6379")
            .withEnv("REDIS_HOST", "redis")
            .waitingFor(Wait.forHttp("/actuator/health"))
            .withStartupTimeout(Duration.of(600, ChronoUnit.SECONDS))
            .dependsOn(redis);

    @BeforeAll
    static void setUp() {
        APP.start();
        port = APP.getFirstMappedPort();
    }

}