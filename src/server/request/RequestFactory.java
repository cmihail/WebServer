package server.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import server.client.Client;
import server.version.HttpVersion;

public class RequestFactory {
	private static final Logger log = Logger.getLogger(RequestFactory.class.getName());
	
	public static Request create(Client client, Socket socket) throws IOException {
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(socket.getInputStream()));
		OutputStream outputStream = socket.getOutputStream();
		
		// Process first line.
		String str = reader.readLine();

		String[] strSplit = str.split("\\s+");
		if (strSplit.length != 3) {
			log.info("Bad request '" + str + "' for " + client);
			return new InvalidRequest(StatusCode._400, outputStream);
		}
		
		// HTTP request is case insensitive.
		String methodStr = strSplit[0].toUpperCase();
		String version = strSplit[2].toUpperCase();
		
		// Files are case-sensitive.
		String uri = strSplit[1];
		
		// Request method.
		Method method = null;
		try {
			method = Method.valueOf(methodStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.info("Bad request '" + str + "' for " + client);
			return new InvalidRequest(StatusCode._400, outputStream);
		}
		
		// Version.
		HttpVersion httpVersion = HttpVersion.valueOfVersion(version.toUpperCase());
		if (httpVersion == null) {
			log.info("Unsupported HTTP version: " + version);
			return new InvalidRequest(StatusCode._505, outputStream);
		}
		
		log.info("New " + method + " request for " + client);
		
		// Construct valid request.
		try {
			switch (method) {
			case HEAD:
				return new HeadRequest(client, reader, outputStream,
						uri, httpVersion);
			case GET:
				return new GetRequest(client, reader, outputStream,
						uri, httpVersion);
			default:
				return new InvalidRequest(StatusCode._501, outputStream);
			}
		} catch(InvalidRequestException e) {
			log.warning("Invalid request for " + client + ". Error: " + e.getMessage());
			return new InvalidRequest(StatusCode._400, outputStream);
		}
	}
}
