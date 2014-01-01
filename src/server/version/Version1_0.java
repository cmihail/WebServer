package server.version;

import java.util.LinkedHashMap;
import java.util.Map;

public class Version1_0 implements VersionHandler {

	private Map<String, String> headers = new LinkedHashMap<String, String>();
	
	public Version1_0(Map<String, String> headers) {
		this.headers = headers;
	}
	
	@Override
	public boolean keepAlive() {
		String value = headers.get("connection"); 
		if (value != null && value.contains("keep-alive"))
			return true;
		return false;
	}

	@Override
	public Map<String, String> getVersionDependentHeaders() {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		if (keepAlive())
			headers.put("Connection", "keep-alive");
		return headers;
	}

	@Override
	public HttpVersion getVersion() {
		return HttpVersion.HTTP_1_0;
	}
}
