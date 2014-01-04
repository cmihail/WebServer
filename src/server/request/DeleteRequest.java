package server.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;

import server.client.Client;
import server.request.helper.FileProcessor;
import server.request.helper.InvalidRequestException;
import server.request.helper.ResponseHeader;
import server.request.helper.StatusCode;
import server.request.version.HttpVersion;

/**
 * A DELETE request. See RFC 2616, section 9.7.
 *
 * @author cmihail
 */
public class DeleteRequest extends GenericRequest {

	protected DeleteRequest(Client client, BufferedReader reader,
			Writer writer, String uri, HttpVersion version) throws IOException,
			InvalidRequestException {
		super(client, reader, writer, uri, version);
	}

	@Override
	protected ResponseHeader processUri(String uri, Map<String, String> headers) {
		StatusCode code;
		Map<String, String> newHeaders = new LinkedHashMap<String, String>();
		
		try {
			File file = FileProcessor.getFile(uri);
			if (!file.exists() || file.isDirectory()) { // Folders can not be removed.
				code = StatusCode._404;
			} else {
				file.delete();
				code = StatusCode._204;
			}
		} catch (AccessDeniedException e) {
			code = StatusCode._403;
		} catch (SecurityException e) {
			code = StatusCode._403;
		}
			
		newHeaders.put("Content-Length", "0");	
		return new ResponseHeader(code, newHeaders);
	}

	@Override
	protected void processBody(BufferedReader reader, Writer writer,
			ResponseHeader response) throws IOException {
		// No need to process body for "DELETE".
	}
}
