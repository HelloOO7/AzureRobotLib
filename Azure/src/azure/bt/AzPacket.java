package azure.bt;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import azure.common.AzInputStream;

public class AzPacket {

	public static final String AZURE_MAGIC = "AZPK";
	public static final String TERM_MAGIC = "TERM";

	protected DataType[] bufferFormats;
	protected Object[] values;

	protected AzPacket(int valueCount) {
		bufferFormats = new DataType[valueCount];
		values = new DataType[valueCount];
	}

	public AzPacket(AzInputStream in) {
		try {
			read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void read(AzInputStream in) throws IOException {
		byte[] buf = new byte[AZURE_MAGIC.length()];
		in.read(buf);
		String magic = new String(buf);
		if (!magic.equals(AZURE_MAGIC)) {
			byte[] rest = new byte[in.available()];
			in.read(rest);

			throw new UnsupportedOperationException(Arrays.toString(buf) + Arrays.toString(rest) + " - The incoming data is not an Azure packet.");
		}

		int bufferCount = in.read();
		bufferFormats = new DataType[bufferCount];
		for (int i = 0; i < bufferCount; i++) {
			bufferFormats[i] = DataType.values()[in.read()];
		}
		values = new Object[bufferCount];
		for (int i = 0; i < bufferCount; i++) {
			DataType data = bufferFormats[i];
			switch (data) {
				case INT16:
					values[i] = in.readShort();
					break;
				case INT32:
					values[i] = in.readInt();
					break;
				case INT8:
					values[i] = in.readByte();
					break;
				case STRING:
					values[i] = in.readUTF();
					break;
			}
		}

		if (!in.getMagic(TERM_MAGIC)) {
			throw new IllegalArgumentException("End of packet expected");
		}
	}

	public void send(DataOutputStream out) throws IOException {
		out.write(AZURE_MAGIC.getBytes());
		out.write(values.length);
		for (DataType d : bufferFormats) {
			out.write(d.ordinal());
		}
		for (int i = 0; i < values.length; i++) {
			Object val = values[i];
			switch (bufferFormats[i]) {
				case INT8:
					out.writeShort((Short) val);
					break;
				case INT16:
					out.writeShort((Short) val);
					break;
				case INT32:
					out.writeInt((Integer) val);
					break;
				case STRING:
					out.writeUTF((String) val);
					break;
			}
		}
		out.write(TERM_MAGIC.getBytes());
	}

	public enum DataType {
		STRING,
		INT32,
		INT16,
		INT8
	}
}
