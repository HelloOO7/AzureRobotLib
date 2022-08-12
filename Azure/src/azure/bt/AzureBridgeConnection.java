package azure.bt;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import azure.common.AzInputStream;
import azure.common.ByteQueueStream;

public class AzureBridgeConnection extends AzureConnection {

	private final ByteQueueStream stream = new ByteQueueStream();
	private DataOutputStream out = new DataOutputStream(new OutStream());

	public AzureBridgeConnection() {
		super();
		recv.bindInputStream(new AzInputStream(stream));
		recv.start();
	}

	@Override
	public String getPartnerName() {
		return "Self";
	}

	@Override
	public boolean getIsConnected() {
		return true;
	}

	@Override
	public synchronized int send(int type, AzPacket packet) throws IOException {
		int size = packet.sizeOf();
		out.writeShort(size);
		out.write(type);
		long start = System.currentTimeMillis();
		int posStart = out.size();
		packet.send(out);
		if (out.size() - posStart != size) {
			throw new RuntimeException("SizeX: " + size + "x" + (out.size() - posStart) + "@" + type);
		}
		return (int) (System.currentTimeMillis() - start);
	}

	@Override
	public void close() throws IOException {

	}

	private class OutStream extends OutputStream {

		@Override
		public void write(int b) throws IOException {
			stream.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			stream.write(b, off, len);
		}
	}
}
