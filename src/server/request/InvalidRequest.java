package server.request;

public class InvalidRequest implements Request {

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean keepAlive() {
		return false;
	}
}
