package start;

import Server.Server;

public class ServerStart {

	public static void main(String[] args) {
		try {
			new Server();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
