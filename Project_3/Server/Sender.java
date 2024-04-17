package Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import Common.MyLogger;
import Common.PacketType;
import Common.Packet;
import Common.Settings;

public class Sender {
	private MyLogger logger = new MyLogger("Server.Sender");
	private PacketManager packetManager;
	private UDP udpSocket;

	public Sender(UDP udpSocket) {
		this.udpSocket = udpSocket;
		this.packetManager = new PacketManager(udpSocket);
	}

	private String fetchFileContent(String fileName) throws IOException {
		fileName = fileName.trim();
		return new String(Files.readAllBytes(Paths.get(fileName)));
	}

	private void dispatchPacket(Packet packet) throws WrongPacketTypeException {
		boolean ackReceived = false;
		while (!ackReceived) {
			String data = new String(packet.getData());
			packetManager.send(packet.getType(), data);
			try {
				packetManager.waitForAcknowledgement();
				ackReceived = true; // ACK received, exit the loop
			} catch (Exception e) {
				logger.error("Timeout occurred while waiting for ACK. Retransmitting packet...");
				packetManager.send(packet.getType(), data); // Retransmit the packet
			}
		}
	}

	public void sendFile(String fileName) throws WrongPacketTypeException, IOException {
		try {
			String fileData = fetchFileContent(fileName);
			int totalBytesSent = 0;

			logger.info("Initiating File Transmission: " + fileName + ", Size: " + fileData.length() + "(bytes)");

			// Send packets
			int startIndex = 0;
			while (startIndex < fileData.length()) {
				int endIndex = Math.min(startIndex + Settings.PACKET_DATA_SIZE, fileData.length());
				String packetData = fileData.substring(startIndex, endIndex);
				Packet packet = new Packet(PacketType.DAT, startIndex, packetData.length(), packetData.getBytes());
				dispatchPacket(packet);
				totalBytesSent += packetData.length();
				startIndex = endIndex;
			}

			dispatchEndOfTransmission();

			logger.info("Data transmission complete, awaiting outstanding ACKs");

			// Wait for ACKs for all sent packets
			while (!packetManager.done()) {
				packetManager.processAck();
			}

			// Close the UDP socket after all ACKs are received
			udpSocket.close();

			logger.info("File Sent. Total Bytes: " + totalBytesSent + "(bytes)");
			packetManager.stats();
		} catch (IOException | WrongPacketTypeException e) {
			logger.error("Error occurred while sending file: " + e.getMessage());
			throw e;
		}
	}

	private void dispatchEndOfTransmission() throws WrongPacketTypeException {
		packetManager.send(PacketType.EOT, "");
		packetManager.waitForAcknowledgement(); // Wait for ACK for EOT packet
	}
}
