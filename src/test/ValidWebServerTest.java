package test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.Constants;
import server.WebServer;

/**
 * Test web server basic functionality.
 * 
 * @author cmihail
 */
public class ValidWebServerTest {
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
	
	@Test(timeout = 500)
	public void testBadRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 400 Bad Request");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest("INVALID_METHOD /index.html HTTP/1.1");
		Runner.runClient(new HeadersTest(req, new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest("GET /index.html HTTP/1.1 one_more");
		Runner.runClient(new HeadersTest(req, new HashSet<String>(expectedLines)), PORT);

		Runner.runClient(new HeadersTest(Runner.constructRequest(""), expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testInvalidMethodRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 505 HTTP Version not supported");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest("HEAD /index.html HTTP/1.2");
		Runner.runClient(new HeadersTest(req ,new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest("GET /index.html HTTR");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testInvalidHttpVersionRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 501 Not Implemented");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest("CONNECT /index.html HTTP/1.1");
		Runner.runClient(new HeadersTest(req, new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest("OPTIONS /index.html HTTP/1.0");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testNoFileHeadRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("HEAD invalidFile HTTP/1.1");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testNoFileGetRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("GET invalidFile HTTP/1.1");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testForbiddenPathRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 403 Forbidden");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("GET .. HTTP/1.1");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testHeadRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Type: text/css");
		expectedLines.add("Content-Length: " + new File(Constants.ROOT, "common.css").length());
		
		String req = Runner.constructRequest("head /common.css HTTP/1.1");
		Runner.runClient(new HeadersTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testGetFileRequest() {
		File file = new File(Constants.ROOT, "index.html");
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + file.length());
 		
		String req = Runner.constructRequest("GET /index.html   HTTP/1.1");
		Runner.runClient(new GetTest(req, expectedLines, file, "index.html"), PORT);
	}
	
	@Test(timeout = 500)
	public void testGetDirectoryRequest() {
		File file = new File(Constants.ROOT, "");
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + file.length());
 		
		String req = Runner.constructRequest("get / HTTP/1.0");
		Runner.runClient(new GetTest(req, expectedLines, file, "/"), PORT);
	}
}
