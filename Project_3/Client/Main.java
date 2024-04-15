package Client;

import Client.Receiver;
import Common.MyLogger;

public class Main {
	public static void main(String[] args) {
		MyLogger logger = new MyLogger("Client.Main");

		logger.info("Receiver started");
		Receiver receiver = new Receiver();

		receiver.getFile(args[0]);
	}
}
