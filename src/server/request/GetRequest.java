package server.request;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import server.Constants;
import server.user.User;
import server.version.HttpVersion;

public class GetRequest extends HeadRequest {
	
	public GetRequest(User user, BufferedReader reader, OutputStream outputStream,
			String uri, HttpVersion version) throws IOException, InvalidRequestException {
		super(user, reader, outputStream, uri, version);
	}

	@Override
	protected void processBody(BufferedReader reader, OutputStream outputStream, Result result)
			throws IOException {
		if (result.getStatusCode() != StatusCode._200) {
			// No body for error case.
			return;
		}
		
		if (result.getFile().isDirectory()) {
			Writer writer = new OutputStreamWriter(outputStream);
			String dirContent =
					FileProcessor.getDirectoryContentAsHtml(result.getFile(), result.getUri());
			writer.write(dirContent.toCharArray());
			
			writer.flush();
			return;
		}
		
		InputStream fileInputStream = new FileInputStream(result.getFile());
		try {
			byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
			while (fileInputStream.available() > 0) {
				outputStream.write(buffer, 0, fileInputStream.read(buffer));
			}
		} finally {
			fileInputStream.close();
		}
		
		outputStream.flush();
	}

}
