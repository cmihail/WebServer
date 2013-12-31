//package server;
//
//import java.io.IOException;
//import java.util.logging.Logger;
//
//import server.WebServer.InvalidPortException;
//
///**
// * TODO mention sources:
// * https://github.com/dasanjos/java-WebServer/
// *
// * @author cmihail
// */
//public class Main {
//	
//	private static final Logger log = Logger.getLogger(Main.class.getName());
//	
//	private static final int DEFAULT_PORT = 5000;
//	
//	public static void main(String args[]) {
//		if (args.length > 1) {
//			System.err.println("Only one optional argument permitted: PORT");
//			return;
//		}
//		
//		int port;
//		if (args.length == 1) {
//			port = Integer.parseInt(args[0]);
//		} else {
//			port = DEFAULT_PORT;
//		}
//
//		try {
//			new WebServer(port).run();
//		} catch (InvalidPortException e) {
//			log.severe(e.getMessage());
//		} catch (IOException e) {
//			log.severe("Could not create request handler for new connection. Error: " + 
//					e.getMessage());
//		}
//	}
//}
