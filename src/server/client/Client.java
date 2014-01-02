package server.client;

/**
 * Define a client information.
 *
 * Because the server doesn't keep need any client related info for now,
 * this class is used only for logging.
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
