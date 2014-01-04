package test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.Constants;
import server.WebServer;
import server.request.FileProcessor;
import server.request.version.HttpVersion;

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
				
				new HeadersTest("", expectedLines).run(reader, writer);
			}
		}, PORT);
	}
	
	@Test (timeout = 500)
	public void testHttpVersion1_0_NoPersistentConnection() throws AccessDeniedException {
		final Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK");
		expectedLines.add("Content-Length: " + FileProcessor.getFile("/index.html").length());
		expectedLines.add("Content-Type: text/html");
		// No "Connection: keep-alive" header.
		
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
	public void testHttpVersion1_1_NoPersistentConnection() throws AccessDeniedException {
		final Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Length: " + FileProcessor.getFile("/index.html").length());
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest(
				"HEAD /index.html HTTP/1.1" + Constants.CRLF + "Connection: close");
		Runner.runClient(new HeadersTest(req, new HashSet<String>(expectedLines)), PORT);
	}
	
	@Test (timeout = 500)
	public void testHttpVersion1_0_PersistentConnection() throws AccessDeniedException {
		Set<String> commonHeaders = new HashSet<String>();
		commonHeaders.add("HTTP/1.0 200 OK");
		commonHeaders.add("Connection: keep-alive");
		persistentConnectionTest(commonHeaders, HttpVersion.HTTP_1_0,
				Constants.CRLF + "Connection: keep-alive");
	}

	@Test (timeout = 500)
	public void testHttpVersion1_1_PersistentConnection() throws AccessDeniedException {
		Set<String> commonHeaders = new HashSet<String>();
		commonHeaders.add("HTTP/1.1 200 OK");
		persistentConnectionTest(commonHeaders, HttpVersion.HTTP_1_1, "");
	}

	public void persistentConnectionTest(Set<String> commonHeaders, HttpVersion version,
			String extraRequest) throws AccessDeniedException {
		final String[] requests = new String[3];
		requests[0] = Runner.constructRequest("HEAD /index.html " + version + extraRequest);
		requests[1] = Runner.constructRequest("HEAD /Earth.png " + version + extraRequest);
		requests[2] = Runner.constructRequest("GET /common.css " + version + extraRequest);
		
		@SuppressWarnings("unchecked")
		final Set<String>[] expectedLines = new Set[3];
		for (int i = 0; i < 3; i++) {
			expectedLines[i] = new HashSet<String>(commonHeaders);
		}

		expectedLines[0].add("Content-Length: " + FileProcessor.getFile("/index.html").length());
		expectedLines[0].add("Content-Type: text/html");
		expectedLines[1].add("Content-Length: " + FileProcessor.getFile("/Earth.png").length());
		expectedLines[1].add("Content-Type: image/png");
		
		File file = FileProcessor.getFile("/common.css");
		expectedLines[2].add("Content-Length: " + file.length());
		expectedLines[2].add("Content-Type: text/css");
		
		final HttpRequestTest[] tests = new HttpRequestTest[3];
		tests[0] = new HeadersTest(requests[0], expectedLines[0]);
		tests[1] = new HeadersTest(requests[1], expectedLines[1]);
		tests[2] = new GetTest(requests[2], expectedLines[2], "/common.css");
		
		Runner.runClient(new HttpRequestTest() {
			@Override
			public void run(BufferedReader reader, Writer writer) throws IOException {
				for (int i = 0; i < requests.length; i++) {
					tests[i].run(reader, writer);
				}
			}
		}, PORT);
	}
	
}
