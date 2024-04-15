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

	public Sender(UDP udpSocket) {
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
				if (packet.getSeqNo() == 0) {
					// If the packet is packet 0, wait for ACK without retransmitting
					packetManager.waitForAcknowledgement();
				} else {
					// For other packets, wait for ACK before sending next packet
					packetManager.waitForAcknowledgement();
				}
				ackReceived = true; // ACK received, exit the loop
			} catch (Exception e) {
				// Timeout occurred, retransmit packet
				logger.error("Timeout occurred while waiting for ACK. Retransmitting packet...");
				if (packet.getSeqNo() != 0) {
					packetManager.send(packet.getType(), data); // Retransmit the packet (except for packet 0)
				}
			}
		}
	}

	public void sendFile(String fileName) throws WrongPacketTypeException, IOException {
		try {
			String fileData = fetchFileContent(fileName);
			int totalBytesSent = 0;

			logger.info("Initiating File Transmission: " + fileName + ", Size: " + fileData.length() + "(bytes)");

			// Send packet 0
			Packet packet0 = new Packet(PacketType.DAT, 0, fileData.length(), fileData.getBytes());
			dispatchPacket(packet0);
			totalBytesSent += fileData.length();

			// Send subsequent packets
			int startIndex = Settings.PACKET_DATA_SIZE;
			while (startIndex < fileData.length()) {
				int endIndex = Math.min(startIndex + Settings.PACKET_DATA_SIZE, fileData.length());
				String packetData = fileData.substring(startIndex, endIndex);
				Packet packet = new Packet(PacketType.DAT, startIndex, packetData.length(), packetData.getBytes());
				dispatchPacket(packet);
				totalBytesSent += packetData.length();
				startIndex = endIndex;
			}

			dispatchEndOfTransmission();

			System.out.println("Data transmission complete, awaiting outstanding ACKs");

			while (!packetManager.done()) {
				packetManager.processAck();
			}

			logger.info("File Sent. Total Bytes: " + totalBytesSent + "(bytes)");
			packetManager.stats();
		} catch (IOException e) {
			logger.error("Error occurred while sending file: " + e.getMessage());
			throw e;
		} catch (WrongPacketTypeException e) {
			logger.error("Error occurred while sending file: " + e.getMessage());
			throw e;
		}
	}

	private void dispatchEndOfTransmission() throws WrongPacketTypeException {
		packetManager.send(PacketType.EOT, "");
		packetManager.waitForAcknowledgement(); // Wait for ACK for EOT packet
	}
}
