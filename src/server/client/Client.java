package server.client;

/**
 * Defines a client information.
 *
 * For now, the server doesn't need any client related information,
 * so this class is used only for logging.
 * 
 * @author cmihail
 */
public class Client {
	
	private long id;
	
	public Client() {
		this.id = Thread.currentThread().getId();
	}

	@Override
	public String toString() {
		return "<client" + id + ">";
	}
}
