package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class WebServer {
	private static final Logger log = Logger.getLogger(WebServer.class.getName());
	
	private final ExecutorService executor;
	private final int port;
	private ServerSocket serverSocket = null;
	
	public class InvalidPortException extends Exception {
		private static final long serialVersionUID = 1L;
		private Exception e;
		
		public InvalidPortException(Exception e) {
			this.e = e;
		}
		
		public String getMessage() {
			return "Could not start server. Error: " + e.getMessage();
		}
	}
	
	// TODO mention that there might be methods for dynamic configuration,
	// like changing persistent connection timeout, etc
	/**
	 * @param port the port on which the server listens for connections
	 */
	public WebServer(int port) throws InvalidPortException {
		executor = Executors.newCachedThreadPool();
		this.port = port;
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new InvalidPortException(e);
		}
		
	}
	
	public void run() throws IOException {
		log.info("Start server on port: " + port);

		while (true) {
			executor.submit(new RequestHandler(serverSocket.accept(),
					Constants.DEFAULT_PERSISTENT_CONNECTION_TIMEOUT));
		}
	}
	
	/**
	 * Should be used from a separate thread to force a server close.
	 * Useful for testing.
	 */
	public void close() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				log.warning("Error at closing server soket: " + e.getMessage());
			}
		}
	}
}
