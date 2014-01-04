package server.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import server.Constants;
import server.client.Client;
import server.request.helper.InvalidRequestException;
import server.request.helper.ResponseHeader;
import server.request.version.HttpVersion;
import server.request.version.Version1_0;
import server.request.version.Version1_1;
import server.request.version.VersionHandler;

/**
 * Defines an abstract implementation of a request.
 * Processes headers and contains common code for most of the requests.
 * Also contains abstract methods that subclasses should implement. 
 * 
 * @author cmihail
 */
public abstract class GenericRequest implements Request {
	private static final Logger log = Logger.getLogger(GenericRequest.class.getName());
	
	private final Map<String, String> headers = new LinkedHashMap<String, String>();
	private final Client client;
	private final BufferedReader reader;
	private final Writer writer;
	private final String uri;
	private final VersionHandler versionHandler;
	
	protected GenericRequest(Client client, BufferedReader reader, Writer writer,
			String uri, HttpVersion version) throws IOException, InvalidRequestException {
		this.client = client;
		this.reader = reader;
		this.writer = writer;
		this.uri = uri;
		
		String str = reader.readLine();
		while (!"".equals(str)) {
			if (str == null) {
				throw new InvalidRequestException("Problem at reading headers");
			}
			
			log.info("New header for " + client + ": " + str);
			
			String[] strSplit = str.split(":\\s*");
			if (strSplit.length != 2) { // Ignore invalid header lines.
				throw new InvalidRequestException("Invalid header line");
			}
			
			// Headers are case insensitive.
			headers.put(strSplit[0].toLowerCase(), strSplit[1].toLowerCase());
			
			str = reader.readLine();
		}
		
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
	 * Processes a URI. headers parameter contains the HTTP headers from the client.
	 * ResponseHeader contains the status code and the headers necessary to write to the client.
	 * Subclasses should memorize in own members all the information needed to process
	 * the body either at this step or in the constructor. processBody() method is
	 * not obligated to provide all information needed for processing, but only the minimal.
	 * 
	 * For HTTP version dependent code: this class does not need to process all HTTP version
	 * dependent headers. If necessary, create a specialized request that doesn't extend
	 * {@link GenericRequest} or that overrides process() method.
	 * 
	 * @param uri a URI to process
	 * @param headers the request headers
	 * @return the result of the URI processing
	 */
	protected abstract ResponseHeader processUri(String uri, Map<String, String> headers);

	/**
	 * Processes the response body (either read from client or write to the client).
	 * Result argument is what processUri() method returned.
	 *
	 * @param reader the socket reader
	 * @param writer the socket writer
	 * @param result the result obtained using method processUri
	 * @throws IOException
	 */
	protected abstract void processBody(BufferedReader reader, Writer writer, ResponseHeader response)
			throws IOException;
	
	@Override
	public void process() throws IOException {
		if (versionHandler == null)
			return;

		ResponseHeader versionResponse = versionHandler.getVersionDependentResponse();
		if (versionResponse.getStatusCode() != null) {
			writer.append(versionHandler.getVersion() + " " + versionResponse.getStatusCode() +
					Constants.CRLF);
			writeHeaders(writer, versionResponse.getHeaders());
			writer.append(Constants.CRLF);
			writer.flush();
			return;
		}
		
		ResponseHeader response = processUri(uri, headers);
		writer.append(versionHandler.getVersion() + " " + response.getStatusCode() +
				Constants.CRLF);
		 
		writeHeaders(writer, versionResponse.getHeaders());
		writeHeaders(writer, response.getHeaders());
		writer.append(Constants.CRLF);
		writer.flush();
		
		processBody(reader, writer, response);
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
}
