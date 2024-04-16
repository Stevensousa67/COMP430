package Server;

import java.io.IOException;
import java.net.*;

import Common.MyLogger;
import Common.Settings;

public class UDP {
	private MyLogger logger = new MyLogger("Server.UDP");

	private DatagramSocket serverSocket;
	private int clientPort = 0;
	private InetAddress clientAddress;

	private static int totalData = 0;
	private static int totalDataWithHeader = 0;

	public void createService() throws SocketException {
		try {
			logger.info("Initializing Server on Port: " + Settings.PORT);
			serverSocket = new DatagramSocket(Settings.PORT);
		} catch (SocketException e) {
			logger.error("Failed to initialize server socket", e);
			throw e;
		}
	}

	public byte[] getPacket(int timeout) throws SocketTimeoutException {
		byte[] receivedData = new byte[Settings.MAX_PACKET_SIZE];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		try {
			serverSocket.setSoTimeout(timeout);
			serverSocket.receive(receivedPacket);
			logger.received(receivedData);
			if (clientPort == 0) {
				clientAddress = receivedPacket.getAddress();
				clientPort = receivedPacket.getPort();
				logger.info("Connection established with client: " + clientAddress + ", Port:" + clientPort);
			}
		} catch (SocketTimeoutException e) {
			throw e;
		} catch (IOException e) {
			logger.error("Failed to receive data from socket.", e);
		}
		return receivedData;
	}

	public void sendPacket(int size, byte[] sendData) {
		totalData += size;
		totalDataWithHeader += size + 12; // Assuming 12-byte header

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
		try {
			logger.sent(sendData);
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			logger.error("Failed to send reply", e);
		}
	}

	public void reset() {
		clientPort = 0;
	}

	public void close() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
	}

	public void stats() {
		logger.info(String.format("Total Data: %d", totalData));
		logger.info(String.format("Total Data Including Packet Headers: %d", totalDataWithHeader));
	}
}
