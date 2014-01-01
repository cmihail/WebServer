package server.request;

public class InvalidRequestException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String message;
	
	public InvalidRequestException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
