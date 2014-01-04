package server.request;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;

import server.Constants;
import server.client.Client;
import server.request.helper.FileProcessor;
import server.request.helper.InvalidRequestException;
import server.request.helper.ResponseHeader;
import server.request.helper.StatusCode;
import server.request.version.HttpVersion;

/**
 * A PUT request. See RFC 2616, section 9.6.
 *  
 * @author cmihail
 */
public class PutRequest extends GenericRequest {

	private int contentLength = 0;
	private File file;

	protected PutRequest(Client client, BufferedReader reader, Writer writer, String uri,
			HttpVersion version) throws IOException, InvalidRequestException {
		super(client, reader, writer, uri, version);
	}

	@Override
	protected ResponseHeader processUri(String uri, Map<String, String> headers) {
		StatusCode code;
		Map<String, String> newHeaders = new LinkedHashMap<String, String>();
			
		if (headers.containsKey("content-length")) {
			try {
				contentLength = Integer.parseInt(headers.get("content-length"));
				
				file = FileProcessor.getFile(uri);
				if (file.isDirectory()) { // Folders can not be overwritten.
					code = StatusCode._404;
				} else if (file.exists()) {
					code = StatusCode._204;
				} else {
					code = StatusCode._201;
				}
			} catch (AccessDeniedException e) {
				code = StatusCode._403;
			} catch (NumberFormatException e) {
				// Content-length is necessary.
				code = StatusCode._411;
			}
		} else {
			// Content-length is necessary.
			code = StatusCode._411;
		}
		
		newHeaders.put("Content-Length", "0");	
		return new ResponseHeader(code, newHeaders);
	}

	@Override
	protected void processBody(BufferedReader reader,  Writer writer, ResponseHeader response)
			throws IOException {
		if (response.getStatusCode() != StatusCode._201 &&
				response.getStatusCode() != StatusCode._204) {
			// No body for error case.
			return;
		}
		
		Writer localWriter = null;
		try {
			localWriter = new OutputStreamWriter(new FileOutputStream(file));
			char[] buffer = new char[Constants.FILE_BUFFER_SIZE];
			while (contentLength > 0) {
				int read = reader.read(buffer);
				if (read < 0) // We should receive contentLength data input.
					throw new EOFException();
				
				contentLength -= read;
				localWriter.write(buffer, 0, read);
			}
		} finally {
			if (localWriter != null)
				localWriter.close();
		}
	}
}
