package server.request;

import java.io.IOException;

/**
 * An HTTP request.
 * 
 * @author cmihail
 */
public interface Request {
	
	/**
	 * Process a request. Needed members should be initialized in the constructor.
	 * @throws IOException 
	 */
	void process() throws IOException;
	
	/**
	 * @return true if connection should be kept alive based on HTTP version / headers
	 */
	boolean keepAlive();
}
