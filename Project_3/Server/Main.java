package Server;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import Common.MyLogger;
import Common.Packet;
import Common.PacketType;
import Common.Settings;
import Server.UDP;
import Server.PacketManager;

public class Main {
	public static void main(String[] args) {
		MyLogger logger = new MyLogger("Server.main");
		
		logger.info("Starting");
		Packet receivedPacket;
		
		if (args.length > 0) {
			if (args[0].equals("1"))
				Settings.SKIP_FIRST_PACKET = true;
			else if (args[0].equals("2"))
				Settings.SKIP_ALL_PACKETS = true;
		}
		
		logger.info("Skipping First Packet in Window: " + Settings.SKIP_FIRST_PACKET);
		logger.info("Skipping All Packets in Window:  " + Settings.SKIP_ALL_PACKETS);
		
		boolean doIt = true;
		try {
			Sender sender = null;
			while (doIt) {
				UDP udpSocket = new UDP();
				udpSocket.createService();
				try {
					byte[] b = udpSocket.getPacket(0);
					receivedPacket = new Packet(b);
					if (receivedPacket.getType() != PacketType.REQ) {
						(new PacketManager(udpSocket)).send(PacketType.ERR,
								"Received " + PacketType.toString(receivedPacket.getType()) + 
								", not sure what that command does at this stage.");
						continue;
					}
				} catch (SocketTimeoutException e) {
					continue;
				}
				
				//if (sender == null)
				sender = new Sender(udpSocket);

				try {
					sender.sendFile(new String(receivedPacket.getData()));
					udpSocket.close();
					
					doIt = false;				
				} catch (IOException e) {
					PacketManager pm = new PacketManager(udpSocket);
					pm.send(PacketType.ERR, "No such file or permission denied.");
					pm.processAck();
					udpSocket.close();
				}
				
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongPacketTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
