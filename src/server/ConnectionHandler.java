package server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import server.request.InvalidRequest;
import server.request.Request;
import server.request.RequestFactory;
import server.request.StatusCode;
import server.user.User;

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
		User user = new User();
		try {
			log.info("New connection from " + user);
			clientSocket.setSoTimeout(timeout);
			
			while (!clientSocket.isClosed()) {
				log.info("Process request for " + user);
				
				Request request = RequestFactory.create(user, clientSocket);
				request.process();
				
				log.info("Finish request for " + user);
				
				if (!request.keepAlive()) {
					log.info("Close connection for " + user);
					break;
				}
			}
		} catch (SocketTimeoutException e) {
			log.info("Connection for " + user + " has reached the timeout limit");
			// TODO write tests for this (also test if clientSocket closes)
			try {
				Request request =
						new InvalidRequest(StatusCode._408, clientSocket.getOutputStream());
				request.process();
			} catch (IOException e1) {
				log.warning("Connection for " + user + " has an error: " + e.getMessage());
			}
		} catch (IOException e) {
			log.warning("Connection for " + user + " has an error: " + e.getMessage());
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				log.warning("Connection for " + user + " has an error: " + e.getMessage());
			}			
		}
	}
}
