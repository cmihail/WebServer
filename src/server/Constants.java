package server;

public class Constants {
	public static final String ROOT = "filesRoot";
	public static final String CRLF = "\r\n";
	public static final int FILE_BUFFER_SIZE = (int) Math.pow(2, 14); // 16KB
	public static final int DEFAULT_PERSISTENT_CONNECTION_TIMEOUT = 5000;
}
