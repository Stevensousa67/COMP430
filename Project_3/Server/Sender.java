package Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import Common.MyLogger;
import Common.PacketType;
import Common.Settings;

public class Sender {
	private MyLogger logger = new MyLogger("Server.Sender");

	public Sender(UDP udpSocket) {
		packetManager = new PacketManager(udpSocket);
	}

	private String getFile(String fn) throws IOException {
		fn = fn.trim();
		return new String(Files.readAllBytes(Paths.get(fn)));
	}

	private void sendData(String data) throws WrongPacketTypeException {
		packetManager.send(PacketType.DAT, data);
	}

	private void sendEOT() throws WrongPacketTypeException {
		packetManager.send(PacketType.EOT, "");
	}

	public void sendFile(String fn) throws WrongPacketTypeException, IOException {
		String fileData = getFile(fn);
		int totalBytes = 0;

		logger.info("Sending File: " + fn + ", Size: " + fileData.length() + "(bytes)");
		while (fileData != "") {
			try {
				sendData(fileData.substring(0, Settings.PACKET_DATA_SIZE));
				totalBytes += fileData.substring(0, Settings.PACKET_DATA_SIZE).length();
				fileData = fileData.substring(Settings.PACKET_DATA_SIZE);
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				sendData(fileData);
				totalBytes += fileData.length();
				fileData = "";
			}
		}
		
		sendEOT();
		
		System.out.println("Data transmission complete, waiting for outstanding ACKs");
		// Wait for transfer to complete.  
		// Keep looping checking packetManager.done, as long as that is false, we should process an ACK
		while (/*...*/) {
			// hint: process the ack here
		}
		
		logger.info("File sent. " + totalBytes + "(bytes)");
		
		// dump the stats
		// ...
		
	}

	private PacketManager packetManager;
}
