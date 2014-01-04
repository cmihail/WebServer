package server.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;

import server.client.Client;
import server.request.version.HttpVersion;

/**
 * A HEAD request. See RFC 2616, section 9.4.
 *  
 * @author cmihail
 */
public class HeadRequest extends GenericRequest {

	public HeadRequest(Client user, BufferedReader reader, Writer writer,
			String uri, HttpVersion version) throws IOException,InvalidRequestException {
		super(user, reader, writer, uri, version);
	}

	@Override
	protected ResponseHeader processUri(String uri, Map<String, String> headers) {
		StatusCode code;	
		File file = null;
		Map<String, String> newHeaders = new LinkedHashMap<String, String>();
		try {
			file = FileProcessor.getFile(uri);
			
			if (file.exists() && !file.isHidden()) {
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
		
		return new ResponseHeader(code, newHeaders);
	}

	@Override
	protected void processBody(BufferedReader reader, Writer writer, ResponseHeader response)
			throws IOException {
		// No need to process body for "HEAD".
	}
}
