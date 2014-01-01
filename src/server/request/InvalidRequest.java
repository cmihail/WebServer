package server.request;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import server.Constants;
import server.version.HttpVersion;

/**
 * Creates an invalid request with a given HTTP status code.
 * Also closes the connection because the request is invalid.
 *
 * @author cmihail
 */
public class InvalidRequest implements Request {

	private Writer writer;
	private StatusCode code;
	
	/**
	 * @param code the invalid request HTTP status code
	 * @param outputStream the stream were to write the response
	 */
	public InvalidRequest(StatusCode code, OutputStream outputStream) {
		writer = new OutputStreamWriter(outputStream);
		this.code = code;
	}
	
	@Override
	public void process() throws IOException {
		 writer.write(HttpVersion.HTTP_1_1.toString() + " " + code + Constants.CRLF);
		 writer.write("Connection: close" + Constants.CRLF + Constants.CRLF);
		 writer.flush();
	}

	@Override
	public boolean keepAlive() {
		return false;
	}
}
