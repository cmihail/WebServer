package test;

import static org.junit.Assert.*;

import org.junit.Test;

import server.WebServer;
import server.WebServer.InvalidPortException;

/**
 * Test invalid web server configurations.
 * 
 * @author cmihail
 */
public class InvalidWebServerTest {

	@Test
	public void testForbiddenPort() {
		invalidPort(10);
	}
	
	@Test
	public void testOutOfLimitsPort() {
		invalidPort(-1);
		invalidPort(70000);
	}
	
	private void invalidPort(int port) {
		try {
			new WebServer(port);
		} catch (IllegalArgumentException e) {
			return;
		} catch (InvalidPortException e) {
			return;
		}
		fail();
	}
}
