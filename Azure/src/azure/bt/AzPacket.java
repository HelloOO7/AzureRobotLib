package azure.bt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import azure.common.AzInputStream;
import lejos.nxt.Button;

public class AzPacket {

	public static final String AZURE_MAGIC = "AZPK";
	public static final String TERM_MAGIC = "TERM";

	protected DataType[] bufferFormats;
	protected Object[] values;

	protected AzPacket() {
	}

	public AzPacket(AzInputStream in) {
		try {
			read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int sizeOf() {
		if (bufferFormats.length != values.length) {
			throw new RuntimeException("PkFmtErr1");
		}
		int len = AZURE_MAGIC.length() + 1 + bufferFormats.length + TERM_MAGIC.length();
		int index = 0;
		for (DataType t : bufferFormats) {
			switch (t) {
			case INT16:
				len += 2;
				break;
			case INT32:
				len += 4;
				break;
			case INT8:
				len++;
				break;
			case STRING:
				len += getUTFLen((String) values[index]);
				break;
			}
			index++;
		}
		return len;
	}

	private int getUTFLen(String str) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(str.length() + 10);
		try {
			new DataOutputStream(out).writeUTF(str);
		} catch (IOException e) {
		}
		return out.size();
	}

	public void read(AzInputStream in) throws IOException {
		byte[] buf = new byte[AZURE_MAGIC.length()];
		in.read(buf);
		String magic = new String(buf);
		if (!magic.equals(AZURE_MAGIC)) {
			byte[] rest = new byte[in.available()];
			in.read(rest);

			throw new UnsupportedOperationException(
					Arrays.toString(buf) + Arrays.toString(rest) + " - The incoming data is not an Azure packet.");
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
		byte[] termBuf = new byte[TERM_MAGIC.length()];
		in.read(termBuf);
		String term = new String(termBuf);
		if (!term.equals(TERM_MAGIC)) {
			Button.waitForAnyPress();
			throw new IllegalArgumentException("BADTERM " + termBuf[0] + '|' + termBuf[1] + '|' + termBuf[2] + '|' + termBuf[2]);
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
				out.write(((Number) val).byteValue());
				break;
			case INT16:
				out.writeShort(((Number) val).shortValue());
				break;
			case INT32:
				out.writeInt(((Number) val).intValue());
				break;
			case STRING:
				out.writeUTF((String) val);
				break;
			}
		}
		out.write(TERM_MAGIC.getBytes());
	}

	public enum DataType {
		STRING, INT32, INT16, INT8
	}
}
