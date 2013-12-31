package server.version;

import java.util.Map;

public interface VersionHandler {
	/**
	 * @return true if connection should be kept alive
	 */
	boolean keepAlive();
	
	/**
	 * @return the version dependent headers
	 */
	Map<String, String> getVersionDependentHeaders();
	
	HttpVersion getVersion();
}
