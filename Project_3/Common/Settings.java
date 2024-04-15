package Common;

public class Settings {
	final static public int PORT = 2015;
	final static public int WINDOW_SIZE = 4;
	final static public int PACKET_DATA_SIZE = 16;
	public static final int MAX_PACKET_SIZE = 2137;
	public static final int TIMEOUT_DURATION = 15000;
	
	static public boolean SKIP_FIRST_PACKET = false;
	static public boolean SKIP_ALL_PACKETS = false;
	
	final static public String LOG_FILE = "UDPFTP.log";
}
