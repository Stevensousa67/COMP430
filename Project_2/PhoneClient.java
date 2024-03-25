/* Author: Steven Sousa - Bridgewater State University - COMP430: Computer Networks - March 1st, 2024 */
package Project_2;

import java.io.*;
import java.net.*;

public class PhoneClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 2014;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to server. Start sending messages...");
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("Server response: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + SERVER_ADDRESS);
        }
    }
}