package test.helper;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import server.Constants;
import server.WebServer;
import server.WebServer.InvalidPortException;

/**
 * @author cmihail
 */
public class Runner {
	
	/**
	 * Run a web server in a different thread.
	 * @param port the server port
	 * @return a reference to the server
	 */
	public static final WebServer runServer(int port) {
		try {
			final WebServer webServer = new WebServer(port);

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
			e.printStackTrace();
			fail();
		}
		return null;
	}
	
	/**
	 * Run a client that tests an HTTP request.
	 * @param httpRequestTest the test
	 */
	public static void runClient(HttpRequestTest httpRequestTest, int port) {
		Socket socket = null;
		try {
			socket = new Socket("localhost", port);

			BufferedReader reader =
					new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Writer writer = new OutputStreamWriter(socket.getOutputStream());

			httpRequestTest.run(reader, writer);
		} catch (Exception e) {
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
	
	/**
	 * Construct a request with the host header inside.
	 * @param head the head of the request
	 * @return the full request
	 */
	public static String constructRequest(String head) {
		StringBuilder request = new StringBuilder(head)
				.append(Constants.CRLF)
				.append("Host: www.example.com")
				.append(Constants.CRLF)
				.append(Constants.CRLF);
		return request.toString();
	}
}
