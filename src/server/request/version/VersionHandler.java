package server.request.version;

import server.request.GenericRequest;
import server.request.helper.ResponseHeader;

/**
 * An HTTP version handler. Necessary for {@link GenericRequest}.
 * Can be ignored if a request needs a more specialized HTTP version dependent
 * implementation than what {@link GenericRequest} provides.
 *
 * @author cmihail
 */
public interface VersionHandler {
	/**
	 * @return the HTTP version
	 */
	HttpVersion getVersion();
	
	/**
	 * @return true if connection should be kept alive
	 */
	boolean keepAlive();
	
	/**
	 * The status code for response header should be null if everything is OK.
	 * @return the version dependent response header
	 */
	ResponseHeader getVersionDependentResponse();
}
