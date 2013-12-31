package server.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;

import server.user.User;
import server.version.HttpVersion;

public class RequestFactory {
	private static final Logger log = Logger.getLogger(RequestFactory.class.getName());
	
	public static Request create(User user, Socket socket) throws IOException {
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		// Process first line.
		String str = reader.readLine();

		String[] strSplit = str.split("\\s+");
		if (strSplit.length != 3) {
			log.info("Invalid request '" + str + "' for " + user);
			return new InvalidRequest();
		}
		
		// Request method.
		Method method = null;
		try {
			method = Method.valueOf(strSplit[0]);
		} catch (IllegalArgumentException e) {
			log.info("Invalid request '" + str + "' for " + user);
			return new InvalidRequest();
		}
		
		// Version.
		HttpVersion httpVersion = HttpVersion.valueOfVersion(strSplit[2]);
		if (httpVersion == null) {
			log.info("Unsupported http version: " + strSplit[2]);
			return new UnsupportedHttpVersionRequest();
		}
		
		log.info("New " + method + " request for " + user);
		
		// Construct valid request.
		switch (method) {
		case HEAD:
			return new HeadRequest(user, reader, socket.getOutputStream(),
					strSplit[1], httpVersion);
		case GET:
			return new GetRequest(user, reader, socket.getOutputStream(),
					strSplit[1], httpVersion);
		default:
			return new NotImplementedRequest();
		}
	}
}
