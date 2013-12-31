package server.version;

import java.util.LinkedHashMap;
import java.util.Map;

public class Version1_1 implements VersionHandler {

	private Map<String, String> headers = new LinkedHashMap<String, String>();
	
	public Version1_1(Map<String, String> headers) {
		this.headers = headers;
	}
	
	@Override
	public boolean keepAlive() {
		String value = headers.get("Connection"); 
		if (value != null && value.contains("close"))
			return false;
		return true;
	}

	@Override
	public Map<String, String> getVersionDependentHeaders() {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		if (!keepAlive())
			headers.put("Connection", "close");
		return headers;
		// TODO Host header is necessary in 1.1
	}
	
	@Override
	public HttpVersion getVersion() {
		return HttpVersion.HTTP_1_1;
	}
}
