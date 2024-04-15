# Reliable UDP File Transfer Project 

This project implements a reliable file transfer system over UDP, addressing the inherent unreliability of UDP by incorporating mechanisms to handle packet loss and ensure successful file delivery.

## Components:

### Client:
- **Receiver.java**: Responsible for requesting and receiving files from the server. It handles acknowledgments, timeouts, and reordering of packets to reconstruct the complete file.

### Server:
- **Sender.java**: Manages sending files to the client, breaking them into packets, and handling retransmissions based on acknowledgments.
- **UDP.java**: Handles low-level UDP communication, including creating the server socket, sending and receiving packets, and managing timeouts.

### Common:
- Contains shared classes and settings used by both client and server, such as packet structure, packet types, and configuration parameters.

## Features:
- **Reliable Data Transfer**: Implements a sliding window mechanism with acknowledgments to ensure all packets are received and in the correct order.
- **Packet Loss Handling**: Detects missing packets through timeouts and acknowledgments, triggering retransmission of lost packets.
- **Error Handling**: Handles potential errors, such as file not found or permission issues, and sends error messages to the client.
- **Logging**: Provides detailed logging of sent and received packets, acknowledgments, and errors for debugging and analysis.

## Running the Project:
- **Compile**: Compile all Java files in the project.
- **Start Server**: Run `java Server.Main [mode]` where `[mode]` is optional and can be:
  1. Simulate loss of the first packet in each window.
  2. Simulate loss of all packets in each window.
- **Start Client**: Run `java Client.Main [filename]` where `[filename]` is the name of the file to request from the server.

## Additional Notes:
- The project assumes both client and server are running on the same machine (localhost).
- The port number and window size can be adjusted in the `Common/Settings.java` file.
- Log files for the client and server are generated in the respective directories.

