package azure.bt;

import java.io.DataOutputStream;
import java.io.IOException;

import azure.bt.packets.DefaultPacket;
import azure.common.AzInputStream;
import lejos.nxt.comm.NXTConnection;

public abstract class AzureNXTConnectionBase extends AzureConnection {
	protected String target;

	private NXTConnection con;

	private DataOutputStream out;
	private AzInputStream in;

	public AzureNXTConnectionBase(String target) {
		this.target = target;
		reopenConnection();
		recv.start();
	}

	@Override
	public String getPartnerName() {
		return target;
	}

	protected abstract NXTConnection openConnection();

	public void reopenConnection() {
		if (con != null) {
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		con = openConnection();
		openStreams();
	}

	private void openStreams() {
		if (con != null) {
			out = con.openDataOutputStream();
			in = new AzInputStream(con.openInputStream());
			recv.bindInputStream(in);
		}
		else {
			throw new RuntimeException("Failed to connect!");
		}
	}

	public boolean getIsConnected() {
		return con != null;
	}

	public int send(String str) throws IOException {
		DefaultPacket pkt = new DefaultPacket(str);

		return send(0, pkt);
	}

	@Override
	public int send(int type, AzPacket packet) throws IOException {
		out.flush();
		out.writeShort(packet.sizeOf());
		out.write(type);
		packet.send(out);
		long start = System.currentTimeMillis();
		out.flush();

		return (int) (System.currentTimeMillis() - start);
	}

	@Override
	public void close() throws IOException {
		con.close();
	}
}