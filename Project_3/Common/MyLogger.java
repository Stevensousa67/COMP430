package Common;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import Common.Packet;

public class MyLogger {
	
	public MyLogger(String name) {
		boolean append = true;
	    FileHandler handler;
	    
		try {
			this.name = name;
			
			if (logger != null)
				return;
			
			// Set the log format style.
		    System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %5$s%6$s%n");
		    
			//handler = new FileHandler(Settings.LOG_FILE, append);
			handler = new FileHandler("C:/Users/sattar/proj3" + name + ".log", append);

		    logger = Logger.getLogger(name);
		    
	        SimpleFormatter formatter = new SimpleFormatter();  

	        handler.setFormatter(formatter);  

		    logger.addHandler(handler);
		    
		    logger.info("Log started for: " + name);
		} catch (SecurityException | IOException e) {
			System.out.println("Unable to create log");
			e.printStackTrace();
		}
	}

	private String fmt(String msg) {
		return "[" + name + "] " + msg.replaceAll("\\p{C}", "");
	}
	
	public void info(String msg) {
		logger.info(fmt(msg));
	}
	
	public void warning(String msg) {
		logger.warning(fmt(msg));
	}

	public void error(String msg, Exception e) {
		logger.log(Level.SEVERE, fmt(msg), e);
	}
	
	public void error(String msg) {
		logger.severe(fmt(msg));
	}

	public Logger getLog() {
	    return logger;		
	}
	
	public void sent(byte[] data) {
		Packet p = new Packet(data);
		logger.info(fmt(
				"Sending   -----> " + PacketType.toString(p.getType()) + " packet " +
				((p.getType() == PacketType.DAT) ? "seqNo: " + p.getSeqNo() + ", containing " + p.getSize() + "(bytes) of the file." : "") +
				((p.getType() == PacketType.ACK) ? "for sequence number: " + p.getSeqNo()  : "") +
				((p.getType() == PacketType.REQ) ? "for file: " + new String(p.getData())  : "") +
				((p.getType() == PacketType.ERR) ? "due to: " + new String(p.getData())  : "")));
	}
	
	public void received(byte[] data) {
		Packet p = new Packet(data);
		logger.info(fmt(
				"Received   <----- " + PacketType.toString(p.getType()) + " packet " +
				((p.getType() == PacketType.DAT) ? "seqNo: " + p.getSeqNo() + ", containing " + p.getSize() + "(bytes) of the file." : "") +
				((p.getType() == PacketType.ACK) ? "for sequence number: " + p.getSeqNo()  : "") +
				((p.getType() == PacketType.REQ) ? "for file: " + new String(p.getData())  : "") +
				((p.getType() == PacketType.ERR) ? "due to: " + new String(p.getData())  : "")));
	}
	
	private static Logger logger = null;
	private String name;
}