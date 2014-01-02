package server.version;

import java.util.Map;

/**
 * An HTTP version handler.
 *
 * @author cmihail
 */
public interface VersionHandler {
	/**
	 * @return true if connection should be kept alive
	 */
	boolean keepAlive();
	
	/**
	 * @return the version dependent headers
	 */
	Map<String, String> getVersionDependentHeaders();
	
	/**
	 * @return the HTTP version
	 */
	HttpVersion getVersion();
}
