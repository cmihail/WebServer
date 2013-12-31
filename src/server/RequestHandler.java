package server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import server.request.Request;
import server.request.RequestFactory;
import server.user.User;

public class RequestHandler implements Runnable {
	private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
	
	private Socket clientSocket;
	private int timeout;
	
	/**
	 * @param socket the client socket
	 * @param timeout the timeout after the socket must be closed
	 */
	public RequestHandler(Socket socket, int timeout) {
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
					clientSocket.close(); // TODO might use Request Timeout (see error code 408)
					log.info("Close connection for " + user);
					break;
				}
			}
		} catch (SocketTimeoutException e) {
			log.info("Connection for " + user + " has reached the timeout limit");

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
