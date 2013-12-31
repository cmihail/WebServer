package server.version;

public enum HttpVersion {
	HTTP_1_0("HTTP/1.0"),
	HTTP_1_1("HTTP/1.1");
	
	private String version;
	
	private HttpVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @param str the HTTO version as string
	 * @return HTTP version constant or null if str is invalid
	 */
	public static HttpVersion valueOfVersion(String str) {
		if (str.equals(HTTP_1_0.version))
			return HTTP_1_0;
		else if (str.equals(HTTP_1_1.version))
			return HTTP_1_1;
		return null;
	}
	
	@Override
	public String toString() {
		return version;
	}
}
