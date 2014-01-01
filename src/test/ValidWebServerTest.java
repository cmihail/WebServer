package test;

import static org.junit.Assert.*;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.Constants;
import server.WebServer;
import server.WebServer.InvalidPortException;

/**
 * Test web server functionality.
 * 
 * @author cmihail
 */
public class ValidWebServerTest {
	private static final int PORT = 10000;
	
	private WebServer webServer;
	
	@Before
	public void bringUp() {
		try {
			webServer = new WebServer(PORT);

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
		} catch (InvalidPortException e) {
			fail();
		}
	}
	
	@After
	public void tearDown() {
		webServer.close();
	}
	
	@Test(timeout = 500)
	public void testBadRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 400 Bad Request");
		expectedLines.add("Connection: close");
		
		runClient(new Headers(constructRequest("INVALID_METHOD /index.html HTTP/1.1"),
				new HashSet<String>(expectedLines)));
		runClient(new Headers(constructRequest("GET /index.html HTTP/1.1 one_more"),
				new HashSet<String>(expectedLines)));
		runClient(new Headers(constructRequest(""), expectedLines));
	}
	
	@Test(timeout = 500)
	public void testInvalidMethodRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 505 HTTP Version not supported");
		expectedLines.add("Connection: close");
		
		runClient(new Headers(constructRequest("HEAD /index.html HTTP/1.2"),
				new HashSet<String>(expectedLines)));
		runClient(new Headers(constructRequest("GET /index.html HTTR"), expectedLines));
	}
	
	@Test(timeout = 500)
	public void testInvalidHttpVersionRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 501 Not Implemented");
		expectedLines.add("Connection: close");
		
		runClient(new Headers(constructRequest("CONNECT /index.html HTTP/1.1"),
				new HashSet<String>(expectedLines)));
		runClient(new Headers(constructRequest("OPTIONS /index.html HTTP/1.0"), expectedLines));
	}
	
	@Test(timeout = 500)
	public void testNoFileHeadRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		runClient(new Headers(constructRequest("HEAD invalidFile HTTP/1.1"), expectedLines));
	}
	
	@Test(timeout = 500)
	public void testNoFileGetRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		runClient(new Headers(constructRequest("GET invalidFile HTTP/1.1"), expectedLines));
	}
	
	@Test(timeout = 500)
	public void testHeadRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + new File(Constants.ROOT, "index.html").length());
		
		runClient(new Headers(constructRequest("HEAD /index.html HTTP/1.1"), expectedLines));
	}
	
	@Test(timeout = 500)
	public void testGetFileRequest() {
		File file = new File(Constants.ROOT, "index.html");
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + file.length());
 		
		runClient(new Get(constructRequest("GET /index.html HTTP/1.1"), expectedLines,
				file, "index.html"));
	}
	
	@Test(timeout = 500)
	public void testGetDirectoryRequest() {
		File file = new File(Constants.ROOT, "");
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + file.length());
 		
		runClient(new Get(constructRequest("GET / HTTP/1.0"), expectedLines, file, "/"));
	}

	/**
	 * @param firstLine the first line in the request
	 * @return the full request
	 */
	private String constructRequest(String firstLine) {
		StringBuilder request = new StringBuilder(firstLine)
				.append(Constants.CRLF)
				.append("Host: www.example.com")
				.append(Constants.CRLF)
				.append(Constants.CRLF);
		return request.toString();
	}
	
	/**
	 * Run a client that tests an HTTP request.
	 * @param httpRequestTest the test
	 */
	private void runClient(HttpRequest httpRequestTest) {
		Socket socket = null;
		try {
			socket = new Socket("localhost", PORT);

			BufferedReader reader =
					new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Writer writer = new OutputStreamWriter(socket.getOutputStream());

			httpRequestTest.run(reader, writer);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
					fail();
				}
			}
		}
	}
}
