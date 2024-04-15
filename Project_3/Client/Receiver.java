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
		Packet requestPacket = prepareRequestPacket(fileName);

		try {
			receiveAndProcessData(requestPacket);
		} catch (Exception e) {
			handleException(e);
		}
	}

	private Packet prepareRequestPacket(String fileName) {
		return new Packet(PacketType.REQ, 0, fileName.length(), fileName.getBytes());
	}

	private void receiveAndProcessData(Packet requestPacket) throws Exception {
		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(Settings.TIMEOUT_DURATION);

		InetAddress serverAddress = InetAddress.getByName("localhost");
		DatagramPacket initialPacket = prepareInitialPacket(requestPacket, serverAddress);

		clientSocket.send(initialPacket);
		logger.sent(initialPacket.getData());

		String fileContent = "";
		int totalDataSize = 0;
		boolean endOfFile = false;
		int expectedSeqNo = 0;

		while (!endOfFile) {
			try {
				DatagramPacket receivedPacket = receivePacket(clientSocket);
				logger.received(receivedPacket.getData());

				Packet receivedPacketObj = new Packet(receivedPacket.getData());

				if (receivedPacketObj.getType() == PacketType.ERR) {
					logger.info("An error occurred: " + new String(receivedPacketObj.getData()));
					return;
				}

				if (receivedPacketObj.getSeqNo() == expectedSeqNo) {
					fileContent += new String(receivedPacketObj.getData());
					totalDataSize += receivedPacketObj.getSize();

					if (receivedPacketObj.getType() == PacketType.EOT) {
						endOfFile = true;
					}

					sendAcknowledgement(expectedSeqNo, serverAddress, clientSocket);
					expectedSeqNo = (expectedSeqNo + 1) % 4; // Move to the next expected sequence number
				}
			} catch (SocketTimeoutException e) {
				logger.info("Timeout occurred while waiting for packet.");
				// If packet expectedSeqNo is not received within timeout, retransmit the
				// ACK for the current expected sequence number
				sendAcknowledgement(expectedSeqNo, serverAddress, clientSocket);
			}
		}

		clientSocket.close();
		logger.info("File transfer complete. Total data size: " + totalDataSize + " bytes.");
		System.out.println(fileContent);
	}

	private DatagramPacket prepareInitialPacket(Packet requestPacket, InetAddress serverAddress) {
		return new DatagramPacket(requestPacket.getBytes(), requestPacket.getBytes().length, serverAddress,
				Settings.PORT);
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
