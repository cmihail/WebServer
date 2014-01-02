package server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import server.client.Client;
import server.request.InvalidRequest;
import server.request.Request;
import server.request.RequestFactory;
import server.request.StatusCode;

/**
 * Handle a HTTP request.
 * 
 * @author cmihail
 */
public class ConnectionHandler implements Runnable {
	private static final Logger log = Logger.getLogger(ConnectionHandler.class.getName());
	
	private Socket clientSocket;
	private int timeout;
	
	/**
	 * @param socket the client socket
	 * @param timeout the timeout after the socket must be closed
	 */
	public ConnectionHandler(Socket socket, int timeout) {
		clientSocket = socket;
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		// We don't care if there are users with same ids due to thread ids.
		// User might be used for authentication, but we don't really care.
		Client client = new Client();
		try {
			log.info("New connection from " + client);
			clientSocket.setSoTimeout(timeout);
			
			while (!clientSocket.isClosed()) {
				log.info("Process request for " + client);
				
				Request request = RequestFactory.create(client, clientSocket);
				request.process();
				
				log.info("Finish request for " + client);
				
				if (!request.keepAlive()) {
					log.info("Close connection for " + client);
					break;
				}
			}
		} catch (SocketTimeoutException e) {
			log.info("Connection for " + client + " has reached the timeout limit");
			try {
				Request request =
						new InvalidRequest(StatusCode._408, clientSocket.getOutputStream());
				request.process();
			} catch (IOException e1) {
				log.warning("Connection for " + client + " has an error: " + e.getMessage());
			}
		} catch (IOException e) {
			log.warning("Connection for " + client + " has an error: " + e.getMessage());
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				log.warning("Connection for " + client + " has an error: " + e.getMessage());
			}			
		}
	}
}
