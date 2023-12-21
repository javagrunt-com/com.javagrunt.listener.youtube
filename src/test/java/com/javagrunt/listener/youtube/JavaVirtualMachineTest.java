package com.javagrunt.listener.youtube;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class JavaVirtualMachineTest extends AbstractAppTests {
	
	@LocalServerPort
	private int port;

	@Override
	int getPort() {
		return this.port;
	}
}
