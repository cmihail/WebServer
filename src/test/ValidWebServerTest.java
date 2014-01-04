package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.Constants;
import server.WebServer;
import server.request.helper.FileProcessor;
import test.helper.GetTest;
import test.helper.PutTest;
import test.helper.ResponseTest;
import test.helper.Runner;

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
		Runner.runClient(new ResponseTest(req, new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest("GET /index.html HTTP/1.1 one_more");
		Runner.runClient(new ResponseTest(req, new HashSet<String>(expectedLines)), PORT);

		Runner.runClient(new ResponseTest(Runner.constructRequest(""), expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testInvalidMethodRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 505 HTTP Version not supported");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest("HEAD /index.html HTTP/1.2");
		Runner.runClient(new ResponseTest(req ,new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest("GET /index.html HTTR");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testInvalidHeaderRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 400 Bad Request");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest("HEAD /index.html HTTP/1.1" + Constants.CRLF +
				"Invalid header syntax");
		Runner.runClient(new ResponseTest(req ,new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest("PUT /index.html.new HTTP/1.1" + Constants.CRLF +
				"Invalid : header : syntax");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testInvalidHttpVersionRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 501 Not Implemented");
		expectedLines.add("Connection: close");
		
		String req = Runner.constructRequest("CONNECT /index.html HTTP/1.1");
		Runner.runClient(new ResponseTest(req, new HashSet<String>(expectedLines)), PORT);
		
		req = Runner.constructRequest("OPTIONS /index.html HTTP/1.0");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testNoHostRequestHttpVersion1_1() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 400 Bad Request");
		expectedLines.add("Connection: close");
		
		String req = "HEAD /index.html HTTP/1.1" + Constants.CRLF + Constants.CRLF;
		Runner.runClient(new ResponseTest(req, new HashSet<String>(expectedLines)), PORT);
	}
	
	@Test(timeout = 500)
	public void testNoHostRequestHttpVersion1_0() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK"); // For HTTP1.0 it's OK to not have Host.
		expectedLines.add("Content-Length: 79");
		expectedLines.add("Content-Type: text/html");
		
		String req = "HEAD /index.html HTTP/1.0" + Constants.CRLF + Constants.CRLF;
		Runner.runClient(new ResponseTest(req, new HashSet<String>(expectedLines)), PORT);
	}
	
	@Test(timeout = 500)
	public void testNoFileHeadRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("HEAD invalidFile HTTP/1.1");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testNoFileGetRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("GET invalidFile HTTP/1.1");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testForbiddenPathRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 403 Forbidden");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("GET .. HTTP/1.1");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testNoContentLengthPutRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 411 Length Required");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("PUT /index.html.new HTTP/1.1");
		Runner.runClient(new PutTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testOverwriteDirPutRequest() {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("PUT /innerFolder HTTP/1.1" + Constants.CRLF +
				"Content-Length: 10");
		Runner.runClient(new PutTest(req, expectedLines), PORT);
	}
	
	@Test (timeout = 500)
	public void testDeleteDirectoryRequest() throws IOException {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 404 Not Found");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("DELETE /innerFolder HTTP/1.1");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
		
		assertTrue(FileProcessor.getFile("/innerFolder").exists());
	}
	
	@Test(timeout = 500)
	public void testHeadRequest() throws AccessDeniedException {
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Type: text/css");
		expectedLines.add("Content-Length: " + FileProcessor.getFile("/common.css").length());
		
		String req = Runner.constructRequest("head /common.css HTTP/1.1");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
	}
	
	@Test(timeout = 500)
	public void testGetFileRequest() throws AccessDeniedException {
		File file = FileProcessor.getFile("index.html");
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + file.length());
		
		String req = Runner.constructRequest("GET index.html   HTTP/1.1");
		Runner.runClient(new GetTest(req, expectedLines, "index.html"), PORT);
	}
	
	@Test(timeout = 500)
	public void testGetDirectoryRequest()throws AccessDeniedException {
		File file = FileProcessor.getFile("/");
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.0 200 OK");
		expectedLines.add("Content-Type: text/html");
		expectedLines.add("Content-Length: " + file.length());
 		
		String req = Runner.constructRequest("get / HTTP/1.0");
		Runner.runClient(new GetTest(req, expectedLines, "/"), PORT);
	}
	
	@Test(timeout = 500)
	public void testPutRequest() throws AccessDeniedException {
		File originFile = FileProcessor.getFile("/index.html");
		File newFile = FileProcessor.getFile("/index.html.new");
		newFile.delete(); // Make sure the file doesn't exist.
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 201 Created");
		expectedLines.add("Content-Length: 0");
		
		String req = Runner.constructRequest("PUT /index.html.new HTTP/1.1" + Constants.CRLF +
				"Content-Length: " + originFile.length());
		Runner.runClient(new PutTest(req, expectedLines, originFile, newFile), PORT);
		
		expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 204 No Content");
		expectedLines.add("Content-Length: 0");
		
		Runner.runClient(new PutTest(req, expectedLines, originFile, newFile), PORT);
		
		newFile.delete();
	}
	
	@Test (timeout = 500)
	public void testDeleteRequest() throws IOException {
		File file = FileProcessor.getFile("/newFile");
		file.createNewFile();
		
		Set<String> expectedLines = new HashSet<String>();
		expectedLines.add("HTTP/1.1 204 No Content");
		expectedLines.add("Content-Length: 0");
		
		assertTrue(file.exists());
		
		String req = Runner.constructRequest("DELETE /newFile HTTP/1.1");
		Runner.runClient(new ResponseTest(req, expectedLines), PORT);
		
		assertFalse(file.exists());
	}
}
