package server.request.version;

import java.util.LinkedHashMap;
import java.util.Map;

import server.request.ResponseHeader;
import server.request.StatusCode;

/**
 * @author cmihail
 */
public class Version1_1 implements VersionHandler {

	private Map<String, String> headers = new LinkedHashMap<String, String>();
	
	public Version1_1(Map<String, String> headers) {
		this.headers = headers;
	}
	
	@Override
	public boolean keepAlive() {
		if (!headers.containsKey("host"))
			return false;

		String value = headers.get("connection");
		if (value != null && value.contains("close"))
			return false;

		return true;
	}

	@Override
	public ResponseHeader getVersionDependentResponse() {
		Map<String, String> responseHeaders = new LinkedHashMap<String, String>();
		if (!keepAlive())
			responseHeaders.put("Connection", "close");
		
		if (!headers.containsKey("host"))
			return new ResponseHeader(StatusCode._400, responseHeaders);
			
		return new ResponseHeader(responseHeaders);
	}
	
	@Override
	public HttpVersion getVersion() {
		return HttpVersion.HTTP_1_1;
	}
}
