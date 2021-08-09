package azure.bt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import lejos.nxt.comm.NXTConnection;

public class ConnectionOutputStream extends OutputStream {

	private NXTConnection con;

	public ConnectionOutputStream(NXTConnection con) {
		this.con = con;
	}

	private byte[] onebyteTemp = new byte[1];

	@Override
	public synchronized void write(int b) throws IOException {
		con.write(onebyteTemp, 1);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if (off > 0) {
			con.write(Arrays.copyOfRange(b, off, off + len), len);
		}
		else {
			System.out.println(con.write(b, len));
		}
	}

}
