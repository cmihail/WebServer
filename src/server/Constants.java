package server;

/**
 * Global constants definitions.
 * 
 * @author cmihail
 */
public class Constants {
	public static final String ROOT = "filesRoot";
	public static final String CRLF = "\r\n";
	public static final int FILE_BUFFER_SIZE = (int) Math.pow(2, 14); // 16 KB
	public static final int DEFAULT_PORT = 5000;
	public static final int DEFAULT_PERSISTENT_CONNECTION_TIMEOUT = 5000; // in ms
}
