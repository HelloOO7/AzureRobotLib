package azstudio;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AzInputStream extends DataInputStream {
	public AzInputStream(InputStream in) {
		super(in);
	}

	public String readString() throws IOException{
		StringBuilder sb = new StringBuilder();
		char c;
		while ((c = (char)in.read()) != 0x00){
			sb.append(c);
		}
		if (sb.length() == 0) {
			return null;
		}
		return sb.toString();
	}

	public boolean getMagic(String magic) throws IOException {
		byte[] buf = new byte[magic.length()];
		in.read(buf);

		//Charset is irrelevant on NXJ
		return new String(buf).equals(magic);
	}

	public byte[] readSizedArray(int size) throws IOException {
		byte[] arr = new byte[size];
		read(arr);
		return arr;
	}
}
