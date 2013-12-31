package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import server.Constants;
import server.WebServer;
import server.WebServer.InvalidPortException;

public class WebServerTest extends TestCase {
	
	private static final int VALID_PORT = 10002;

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
	
	@Test
	public void testInvalidHeadRequest() {
		WebServer webServer = startServer();
		
		StringBuilder request = new StringBuilder("HEAD invalidFile HTTP/1.0")
				.append(Constants.CRLF)
				.append("Host: www.example.com")
				.append(Constants.CRLF)
				.append(Constants.CRLF);
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		runClient(request.toString(), expectedLines);
		webServer.close();
	}
	
	@Test
	public void testGetRequest() {
		WebServer webServer = startServer();
		
		StringBuilder request = new StringBuilder("HEAD /index.html HTTP/1.0")
				.append(Constants.CRLF)
				.append("Host: www.example.com")
				.append(Constants.CRLF)
				.append(Constants.CRLF);
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + new File(Constants.ROOT, "index.html").length());
		
		runClient(request.toString(), expectedLines);
		webServer.close();
	}
	
	private void runClient(String request, Set<String> expectedLines) {
		Socket socket = null;
		try {
			socket = new Socket("localhost", VALID_PORT);
			
			Writer writer = new OutputStreamWriter(socket.getOutputStream());
			writer.write(request);
			writer.flush();
			
			BufferedReader reader =
					new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String str = reader.readLine();
			while (str != null && !"".equals(str)) {
				System.out.println(str); // TODO
				
				assertTrue(expectedLines.contains(str));
				expectedLines.remove(str);
				
				str = reader.readLine();
			}
			
			assertTrue(expectedLines.isEmpty());
		} catch (UnknownHostException e) {
			fail();
		} catch (IOException e) {
			fail();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					fail();
				}
			}
		}
	}
	
	/**
	 * Start a server in a new thread.
	 * @return the reference the the new server
	 */
	private WebServer startServer() {
		try {
			final WebServer webServer = new WebServer(VALID_PORT);

			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						webServer.run();
					} catch (IOException e) {
						// Expected exception from server forced close.
					}
				}
			}).start();

			return webServer;
		} catch (InvalidPortException e) {
			fail();
		}

		return null;
	}
}
