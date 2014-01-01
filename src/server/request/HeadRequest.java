package server.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;

import server.user.User;
import server.version.HttpVersion;

public class HeadRequest extends GenericRequest {

	public HeadRequest(User user, BufferedReader reader, OutputStream outputStream,
			String uri, HttpVersion version) throws IOException,InvalidRequestException {
		super(user, reader, outputStream, uri, version);
	}

	@Override
	protected Result processUri(String uri, Map<String, String> headers) {
		StatusCode code;	
		File file = null;
		Map<String, String> newHeaders = new LinkedHashMap<String, String>();
		try {
			file = FileProcessor.getFile(uri);
			
			if (file != null && file.exists() && !file.isHidden()) {
				code = StatusCode._200;
				newHeaders.put("Content-Length", Long.toString(file.length()));
				
				// Directory is listed as a HTML file.
				ContentType type = file.isDirectory() ?
						ContentType.HTML :  ContentType.getContentType(uri); 
				newHeaders.put("Content-Type", type.toString());
			} else {
				code = StatusCode._404;
				newHeaders.put("Content-Length", "0");
			}
		} catch (AccessDeniedException e) {
			code = StatusCode._403;
			newHeaders.put("Content-Length", "0");
		}
		
		return new Result(uri, code, newHeaders, file);
	}

	@Override
	protected void processBody(BufferedReader reader, OutputStream outputStream, Result result)
			throws IOException {
		// No need to process body for "HEAD".
	}
}
