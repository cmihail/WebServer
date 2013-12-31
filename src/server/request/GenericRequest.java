package server.request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import server.Constants;
import server.user.User;
import server.version.HttpVersion;
import server.version.Version1_0;
import server.version.Version1_1;
import server.version.VersionHandler;

public abstract class GenericRequest implements Request {
	// TODO requests are case-insensitive, add this
	private static final Logger log = Logger.getLogger(GenericRequest.class.getName());
	
	private final Map<String, String> headers = new LinkedHashMap<String, String>();
	private final VersionHandler versionHandler;
	private final BufferedReader reader;
	private final OutputStream outputStream;
	private final String uri;
	
	protected GenericRequest(User user, BufferedReader reader, OutputStream outputStream,
			String uri, HttpVersion version) throws IOException {
		this.reader = reader;
		this.outputStream = outputStream; // TODO maybe writer directly
		this.uri = uri;
		
		String str;
		do {
			str = reader.readLine();
			if (str == null) {
				log.warning("Invalid request");
				// TODO should return invalid request
				versionHandler = null;
				return;
			}
			
			log.info("New header for " + user + ": " + str);
			
			String[] strSplit = str.split(":\\s*");
			// Ignore invalid header lines or new line.
			if (strSplit.length != 2) {
				continue;
			}
			
			// TODO see if same header value can be on multiple lines
			headers.put(strSplit[0], strSplit[1]);
		} while (!"".equals(str));
		
		switch (version) {
		case HTTP_1_0:
			versionHandler = new Version1_0(headers);
			break;
		case HTTP_1_1:
			versionHandler = new Version1_1(headers);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * @author cmihail
	 */
	protected class Result {
		private final String uri;
		private final StatusCode statusCode;
		private final Map<String, String> newHeaders;
		private final File file;
		
		protected Result(String uri, StatusCode statusCode,
				Map<String, String> newHeaders, File file) {
			this.uri = uri;
			this.statusCode = statusCode;
			this.newHeaders = newHeaders;
			this.file = file;
		}

		protected String getUri() {
			return uri;
		}
		
		protected StatusCode getStatusCode() {
			return statusCode;
		}

		protected Map<String, String> getNewHeaders() {
			return newHeaders;
		}

		protected File getFile() {
			return file;
		}
	}
	
	/**
	 * @param uri
	 * @param headers
	 * @return
	 */
	protected abstract Result processUri(String uri, Map<String, String> headers);

	/**
	 * @param reader
	 * @param outputStream
	 * @param result
	 * @throws IOException
	 */
	protected abstract void processBody(BufferedReader reader, OutputStream outputStream, Result result)
			throws IOException;
	
	@Override
	public void process() throws IOException {
		Result result = processUri(uri, headers);
		Writer writer = new OutputStreamWriter(outputStream);
		
		writer.append(versionHandler.getVersion() + " " + result.getStatusCode() +
				Constants.CRLF);
		
		writeHeaders(writer, versionHandler.getVersionDependentHeaders());
		writeHeaders(writer, result.getNewHeaders());
		writer.flush();
		
		processBody(reader, outputStream, result);
	}

	@Override
	public boolean keepAlive() {
		return versionHandler.keepAlive();
	}
	
	private void writeHeaders(Writer writer, Map<String, String> headers) throws IOException {
		for (Entry<String, String> header : headers.entrySet()) {
			writer.append(header.getKey() + ": " + header.getValue() + Constants.CRLF);
		}
	}
}
