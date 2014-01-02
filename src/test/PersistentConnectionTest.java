package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.Constants;
import server.WebServer;

/**
 * Test web server persistent connection functionality.
 *
 * @author cmihail
 */
public class PersistentConnectionTest {
	private static final int PORT = 10000;
	
	private WebServer webServer;
	
	@Before
	public void bringUp() {
		webServer = Runner.runServer(PORT);
		webServer.setPersistentConnectionTimeout(600);
	}
	
	@After
	public void tearDown() {
		webServer.close();
	}
	
	@Test (timeout = 700)
	public void testWebServerTimeout() {
		webServer.setPersistentConnectionTimeout(300);
		
		final Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 408 Request Time-out");
		expectedLines.add("Connection: close");
		
		Runner.runClient(new HttpRequestTest() {
			@Override
			public void run(BufferedReader reader, Writer writer) throws IOException {
				try {
					Thread.sleep(500); // Forces timeout.
				} catch (InterruptedException e) {
					e.printStackTrace();
					fail();
				}
				
				String str = reader.readLine();
				while (str != null && !"".equals(str)) {
					assertTrue(expectedLines.contains(str));
					expectedLines.remove(str);
					
					str = reader.readLine();
				}
				
				assertTrue(expectedLines.isEmpty());
			}
		}, PORT);
	}
	
	@Test (timeout = 500)
	public void testHttpVersion1_0_NoPersistentConnection() {
		final Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK");
		expectedLines.add("Content-Length: " + new File(Constants.ROOT, "index.html").length());
		expectedLines.add("Content-Type: text/html");
		// No "Connection: keep-alive header".
		
		String req = Runner.constructRequest("HEAD /index.html HTTP/1.0");
		Runner.runClient(new HeadersTest(req, new HashSet<String>(expectedLines)), PORT);

		req = Runner.constructRequest(
				"head /index.html HTTP/1.0" + Constants.CRLF + "Connection: close");
		Runner.runClient(new HeadersTest(req, new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest(
				"HEAD /index.html HTTP/1.0" + Constants.CRLF + "Connection: invalid");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}

	@Test (timeout = 500)
	public void testHttpVersion1_0_PersistentConnection() {
		final Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK");
		expectedLines.add("Content-Length: " + new File(Constants.ROOT, "index.html").length());
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Connection: keep-alive");
		
		String req = Runner.constructRequest(
				"HEAD /index.html HTTP/1.0" + Constants.CRLF + "Connection: Keep-Alive");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}
	
	@Test (timeout = 500)
	public void testHttpVersion1_1_NoPersistentConnection() {
		final Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Length: " + new File(Constants.ROOT, "index.html").length());
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest(
				"HEAD /index.html HTTP/1.1" + Constants.CRLF + "Connection: close");
		Runner.runClient(new HeadersTest(req, new HashSet<String>(expectedLines)), PORT);
	}
	
	@Test (timeout = 500)
	public void testMultipleRequests() {
		final String[] requests = new String[3];
		requests[0] = Runner.constructRequest("HEAD /index.html HTTP/1.1");
		requests[1] = Runner.constructRequest("HEAD /Earth.png HTTP/1.1");
		requests[2] = Runner.constructRequest("HEAD /common.css HTTP/1.1");
		
		@SuppressWarnings("unchecked")
		final Set<String>[] expectedLines = new Set[3];
		for (int i = 0; i < 3; i++) {
			expectedLines[i] = new HashSet<String>();
			expectedLines[i].add("HTTP/1.1 200 OK");
		}

		expectedLines[0].add("Content-Length: " + new File(Constants.ROOT, "index.html").length());
		expectedLines[0].add("Content-Type: text/html");
		expectedLines[1].add("Content-Length: " + new File(Constants.ROOT, "Earth.png").length());
		expectedLines[1].add("Content-Type: image/png");
		expectedLines[2].add("Content-Length: " + new File(Constants.ROOT, "common.css").length());
		expectedLines[2].add("Content-Type: text/css");
		
		Runner.runClient(new HttpRequestTest() {
			@Override
			public void run(BufferedReader reader, Writer writer) throws IOException {
				
				for (int i = 0; i < requests.length; i++) {
					writer.write(requests[i]);
					writer.flush();
					
					String str = reader.readLine();
					while (str != null && !"".equals(str)) {
						assertTrue(expectedLines[i].contains(str));
						expectedLines[i].remove(str);
						
						str = reader.readLine();
					}
					
					assertTrue(expectedLines[i].isEmpty());
				}
			}
		}, PORT);
	}
}
