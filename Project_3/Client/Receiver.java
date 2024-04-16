package Client;

import java.io.IOException;
import java.net.*;

import Common.MyLogger;
import Common.Packet;
import Common.PacketType;
import Common.Settings;

public class Receiver {
	private MyLogger logger = new MyLogger("Client.Receiver");

	public void getFile(String fileName) {
		try (DatagramSocket clientSocket = new DatagramSocket()) {
			clientSocket.setSoTimeout(Settings.TIMEOUT_DURATION);

			InetAddress serverAddress = InetAddress.getByName("localhost");
			Packet requestPacket = new Packet(PacketType.REQ, 0, fileName.length(), fileName.getBytes());
			DatagramPacket initialPacket = new DatagramPacket(requestPacket.getBytes(), requestPacket.getBytes().length,
					serverAddress, Settings.PORT);

			clientSocket.send(initialPacket);
			logger.sent(initialPacket.getData());

			StringBuilder fileContent = new StringBuilder();
			int totalDataSize = 0;
			int expectedSeqNo = 0;

			while (true) {
				DatagramPacket receivedPacket = receivePacket(clientSocket);
				logger.received(receivedPacket.getData());

				Packet receivedPacketObj = new Packet(receivedPacket.getData());

				if (receivedPacketObj.getType() == PacketType.ERR) {
					logger.info("An error occurred: " + new String(receivedPacketObj.getData()));
					break;
				}

				if (receivedPacketObj.getType() == PacketType.EOT) {
					// Send acknowledgment for the EOT packet
					sendAcknowledgement(receivedPacketObj.getSeqNo(), serverAddress, clientSocket);
					break; // Exit the loop after sending acknowledgment for EOT packet
				}

				if (receivedPacketObj.getSeqNo() == expectedSeqNo) {
					fileContent.append(new String(receivedPacketObj.getData()));
					totalDataSize += receivedPacketObj.getSize();

					sendAcknowledgement(expectedSeqNo, serverAddress, clientSocket);
					expectedSeqNo = (expectedSeqNo + 1) % 4; // Move to the next expected sequence number
				}
			}

			clientSocket.close();
			logger.info("File transfer complete. Total data size: " + totalDataSize + " bytes.");
			System.out.println(fileContent.toString());
		} catch (IOException e) {
			handleException(e);
		}
	}

	private DatagramPacket receivePacket(DatagramSocket socket) throws IOException {
		byte[] receivedData = new byte[Settings.MAX_PACKET_SIZE];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		socket.receive(receivedPacket);
		return receivedPacket;
	}

	private void sendAcknowledgement(int sequenceNumber, InetAddress serverAddress, DatagramSocket socket)
			throws IOException {
		Packet ackPacket = new Packet(PacketType.ACK, sequenceNumber, 0, null);
		DatagramPacket sendPacket = new DatagramPacket(ackPacket.getBytes(), ackPacket.getBytes().length, serverAddress,
				Settings.PORT);
		socket.send(sendPacket);
		logger.sent(sendPacket.getData());
	}

	private void handleException(Exception e) {
		e.printStackTrace();
		logger.info("An exception occurred: " + e.getMessage());
	}
}