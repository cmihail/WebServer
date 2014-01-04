package server.request;

import java.util.Map;

/**
 * A response header consisting in HTTP status code and headers.
 * Does NOT contain any HTTP body.
 * 
 * @author cmihail
 */
public class ResponseHeader {
	private final StatusCode statusCode;
	private final Map<String, String> responseHeaders;
	
	/**
	 * Sets status code to null.
	 * @param responseHeaders the new headers to set
	 */
	public ResponseHeader(Map<String, String> responseHeaders) {
		this(null, responseHeaders);
	}
	
	/**
	 * @param statusCode the HTTP status code
	 * @param responseHeaders the new headers to set
	 */
	public ResponseHeader(StatusCode statusCode, Map<String, String> responseHeaders) {
		this.statusCode = statusCode;
		this.responseHeaders = responseHeaders;
	}

	public StatusCode getStatusCode() {
		return statusCode;
	}

	public Map<String, String> getHeaders() {
		return responseHeaders;
	}
}
