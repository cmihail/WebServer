package server.request;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import server.Constants;
import server.request.version.HttpVersion;

/**
 * Creates an invalid request with a given HTTP status code.
 * Also closes the connection because the request is invalid.
 *
 * @author cmihail
 */
public class InvalidRequest implements Request {

	private static final Logger log = Logger.getLogger(InvalidRequest.class.getName());
	
	private Writer writer;
	private StatusCode code;
	
	/**
	 * @param code the invalid request HTTP status code
	 * @param outputStream the stream were to write the response
	 */
	public InvalidRequest(StatusCode code, Writer writer) {
		this.writer = writer;
		this.code = code;
	}
	
	@Override
	public void process() throws IOException {
		String request =  HttpVersion.HTTP_1_1.toString() + " " + code + Constants.CRLF +
				"Connection: close" + Constants.CRLF;
		writer.write(request + Constants.CRLF);
		writer.flush();
		
		log.info(request);
	}

	@Override
	public boolean keepAlive() {
		return false;
	}
}
