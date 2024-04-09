package Common;

public class PacketType {
	public static String toString(int code) {
		return codes[code];
	}
	
	final static public int ACK = 0;    // ACKnowledgment of a command
	final static public int REQ = 1;    // REQuest a file to be sent from the server
	final static public int DAT = 2;    // DATa from the file in packets
	final static public int ERR = 3;    // ERRor messages
	final static public int EOT = 4;    // End Of Transmission
	
	final static private String[] codes = {"ACK", "REQ", "DAT", "ERR", "EOT"};
}
