/* Author: Steven Sousa - Bridgewater State University - COMP430: Computer Networks - March 1st, 2024 */
package Project_2;

import java.io.*;
import java.net.*;
import java.util.*;

public class PhoneServer {
    private static int port = 2014;
    private static ServerSocket listener = null;
    private static Map<Integer, Socket> clientSockets = new HashMap<>();
    private static Map<Integer, List<String>> clientData = new HashMap<>();
    private static int clientCount = 0;

    public static void main(String[] args) {
        try {
            listener = new ServerSocket(port);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = listener.accept();
                clientCount++;
                clientSockets.put(clientCount, clientSocket);
                System.out.println("Client " + clientCount + " connected: " + clientSocket);
                ClientThread clientThread = new ClientThread(clientSocket, clientCount);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (listener != null) {
                    listener.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientThread extends Thread {
        private Socket socket;
        private int clientNumber;

        public ClientThread(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
        }

        public void run() {
            try {
                handleConnection(socket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleConnection(Socket socket) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("From client " + clientNumber + ": " + inputLine);
                if (inputLine.equals("helo")) {
                    out.println("100 ok");
                } else {
                    processMessage(inputLine, out, clientNumber);
                }
            }
        }

        private void processMessage(String message, PrintWriter out, int clientNumber) {
            String[] parts = message.split(" ");
            if (parts.length < 2) {
                out.println("400 Bad Request");
                return;
            }
            String command = parts[0];
            String name = parts[1];
            List<String> clientDataList = clientData.computeIfAbsent(clientNumber, k -> new ArrayList<>());
            switch (command) {
                case "STORE":
                    if (parts.length < 3) {
                        out.println("400 Bad Request");
                        return;
                    }
                    String number = parts[2];
                    storeNumber(name, number, clientDataList, out);
                    break;
                case "GET":
                    String phoneNumber = getPhoneNumber(name, clientDataList);
                    if (phoneNumber != null) {
                        out.println("200 " + phoneNumber);
                    } else {
                        out.println("300 Not Found");
                    }
                    break;
                case "REMOVE":
                    removeEntry(name, clientDataList, out);
                    break;
                default:
                    out.println("400 Bad Request");
            }
        }

        private void storeNumber(String name, String number, List<String> clientDataList, PrintWriter out) {
            clientDataList.add(name + " " + number);
            out.println("100 ok");
        }

        private String getPhoneNumber(String name, List<String> clientDataList) {
            for (String entry : clientDataList) {
                String[] parts = entry.split(" ");
                if (parts[0].equals(name)) {
                    return parts[1];
                }
            }
            return null;
        }

        private void removeEntry(String name, List<String> clientDataList, PrintWriter out) {
            Iterator<String> iterator = clientDataList.iterator();
            while (iterator.hasNext()) {
                String entry = iterator.next();
                String[] parts = entry.split(" ");
                if (parts[0].equals(name)) {
                    iterator.remove();
                    out.println("100 ok");
                    return;
                }
            }
            out.println("300 Not Found");
        }
    }
}