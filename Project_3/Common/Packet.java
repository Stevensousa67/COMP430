package Common;

import java.nio.ByteBuffer;

public class Packet {
	/*
	 * Packet Constructor: t = type i = seqno j = size of the packet
	 */

	public Packet(byte[] bytes) {
		fromBytes(bytes);
	}

	public Packet(int t, int i, int j, byte abyte[]) {
		type = t;
		seqno = i;
		size = j;
		data = new byte[size];
		data = abyte;
	}

	public byte[] getData() {
		return data;
	}

	public int getSeqNo() {
		return seqno;
	}

	public int getSize() {
		return size;
	}

	public int getType() {
		return type;
	}

	public String toString() {
		return "type: " + type + "seq: " + seqno + " size: " + size + " data: " + new String(data);
	}

	public byte[] getBytes() {
		ByteBuffer buf = ByteBuffer.allocate(size + Integer.SIZE * 3);
		
		buf.putInt(type);
		buf.putInt(seqno);
		buf.putInt(size);
		if (size > 0)
			buf.put(data);
		
		return buf.array();
	}
	
	public void fromBytes(byte[] data) {
		ByteBuffer buf = ByteBuffer.allocate(data.length);
		buf.put(data);
		buf.rewind();
		type = buf.getInt();
		seqno = buf.getInt();
		size = buf.getInt();
		this.data = buf.array();
	}
	
	private int type;
	private int seqno;
	private int size;
	private byte data[];
}