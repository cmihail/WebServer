package server.user;

public class User {
	
	private long id;
	
	public User() {
		this.id = Thread.currentThread().getId();
	}

	@Override
	public String toString() {
		return "<user" + id + ">";
	}
}
