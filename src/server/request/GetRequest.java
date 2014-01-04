package server.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import server.Constants;
import server.client.Client;
import server.request.helper.FileProcessor;
import server.request.helper.InvalidRequestException;
import server.request.helper.ResponseHeader;
import server.request.helper.StatusCode;
import server.request.version.HttpVersion;

/**
 * A GET request. See RFC 2616, section 9.3.
 *  
 * @author cmihail
 */
public class GetRequest extends HeadRequest {
	
	private String uri;
	
	public GetRequest(Client user, BufferedReader reader, Writer writer,
			String uri, HttpVersion version) throws IOException, InvalidRequestException {
		super(user, reader, writer, uri, version);
		this.uri = uri;
	}

	@Override
	protected void processBody(BufferedReader reader, Writer writer, ResponseHeader response)
			throws IOException {
		if (response.getStatusCode() != StatusCode._200) {
			// No body for error case.
			return;
		}
		
		// No need to test the file. processUri() method from HeadRequest should have done that.
		File file = FileProcessor.getFile(uri); 
		
		if (file.isDirectory()) {
			String dirContent =
					FileProcessor.getDirectoryContentAsHtml(file, uri);
			writer.write(dirContent.toCharArray());
			
			writer.flush();
			return;
		}
		
		BufferedReader localReader = null;
		try {
			localReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			char[] buffer = new char[Constants.FILE_BUFFER_SIZE];
			int read;
			while ((read = localReader.read(buffer)) > 0) {
				writer.write(buffer, 0, read);
			}
		} finally {
			if (localReader != null)
				localReader.close();
		}
		
		writer.flush();
	}
}
