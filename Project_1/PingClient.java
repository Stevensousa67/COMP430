package Project_1;
import java.net.*;

public class PingClient {
    public static void main(String[] args) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket(); // Create a UDP socket
        InetAddress serverAddress = InetAddress.getLocalHost(); // Get the server's IP address
        int serverPort = 2014; // Server port number

        byte[] sendData; // Buffer for sending data
        byte[] receiveData = new byte[1024]; // Buffer for receiving data

        // Send 10 ping requests
        for (int i = 0; i < 10; i++) {
            System.out.println();
            // Print out current time and ping attempt number
            long sendTime = System.nanoTime();
            System.out.println("Ping attempt " + (i + 1) + " at time " + sendTime);

            String message = "Ping " + (i + 1); // Message to send to the server
            sendData = message.getBytes(); // Convert message to bytes

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            clientSocket.send(sendPacket); // Send the packet to the server

            // Set a timeout for receiving the response
            clientSocket.setSoTimeout(2000); // Timeout of 1 second

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                clientSocket.receive(receivePacket); // Receive the response from the server
                long receiveTime = System.nanoTime();
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                long rtt = (receiveTime - sendTime) / 1000; // Calculate round trip time in microseconds
                System.out.println("Server response: " + receivedMessage);
                System.out.println("RTT: " + rtt + " microseconds");
            } catch (SocketTimeoutException e) {
                System.out.println("Request timed out");
            }
        }
        System.out.println();
        clientSocket.close(); // Close the socket
    }
}
