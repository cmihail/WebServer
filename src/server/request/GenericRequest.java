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
import server.client.Client;
import server.version.HttpVersion;
import server.version.Version1_0;
import server.version.Version1_1;
import server.version.VersionHandler;

/**
 * Defines an abstract implementation of a request.
 * Processes headers and contains common code for most of the requests.
 * Also contains abstract methods that subclasses should implement 
 * 
 * TODO(cmihail): add support for multiple line headers (see RFC 2616, Section 2.2)
 * 
 * @author cmihail
 */
public abstract class GenericRequest implements Request {
	private static final Logger log = Logger.getLogger(GenericRequest.class.getName());
	
	private final Map<String, String> headers = new LinkedHashMap<String, String>();
	private final Client client;
	private final BufferedReader reader;
	private final OutputStream outputStream;
	private final String uri;
	private final VersionHandler versionHandler;
	
	
	protected GenericRequest(Client client, BufferedReader reader, OutputStream outputStream,
			String uri, HttpVersion version) throws IOException, InvalidRequestException {
		this.client = client;
		this.reader = reader;
		this.outputStream = outputStream;
		this.uri = uri;
		
		String str;
		do {
			str = reader.readLine();
			if (str == null) {
				throw new InvalidRequestException("Problem at reading headers");
			}
			
			log.info("New header for " + client + ": " + str);
			
			String[] strSplit = str.split(":\\s*");
			// Ignore invalid header lines or new line.
			if (strSplit.length != 2) {
				continue;
			}
			
			// Headers are case insensitive.
			headers.put(strSplit[0].toLowerCase(), strSplit[1].toLowerCase());
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
	 * @param uri a URI to process
	 * @param headers the request headers
	 * @return the result of the URI processing
	 */
	protected abstract Result processUri(String uri, Map<String, String> headers);

	/**
	 * @param reader the socket reader
	 * @param outputStream the socket output stream
	 * @param result the result obtained using method processUri
	 * @throws IOException
	 */
	protected abstract void processBody(BufferedReader reader, OutputStream outputStream, Result result)
			throws IOException;
	
	@Override
	public void process() throws IOException {
		if (versionHandler == null)
			return;
		
		Result result = processUri(uri, headers);
		Writer writer = new OutputStreamWriter(outputStream);
		
		writer.append(versionHandler.getVersion() + " " + result.getStatusCode() +
				Constants.CRLF);
		
		writeHeaders(writer, versionHandler.getVersionDependentHeaders());
		writeHeaders(writer, result.getNewHeaders());
		writer.append(Constants.CRLF);
		writer.flush();
		
		processBody(reader, outputStream, result);
	}

	@Override
	public boolean keepAlive() {
		return (versionHandler != null) && versionHandler.keepAlive();
	}
	
	private void writeHeaders(Writer writer, Map<String, String> headers) throws IOException {
		for (Entry<String, String> header : headers.entrySet()) {
			String headerStr = header.getKey() + ": " + header.getValue(); 
			writer.append(headerStr + Constants.CRLF);

			log.info("Output header for " + client + ": " + headerStr);
		}
	}
	
	/**
	 * Helper class for subclasses that implement {@link GenericRequest}.
	 * 
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
}
