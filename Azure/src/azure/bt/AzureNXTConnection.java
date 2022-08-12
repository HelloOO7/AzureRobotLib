package azure.bt;

import java.io.DataOutputStream;
import java.io.IOException;

import azure.bt.packets.DefaultPacket;
import azure.common.AzInputStream;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RS485;

public class AzureNXTConnection extends AzureConnection {
	private final String target;

	private NXTConnection con;

	private DataOutputStream out;
	private AzInputStream in;

	private AzConnectionType type;

	public AzureNXTConnection(String target) {
		this(target, AzConnectionType.BLUETOOTH);
	}

	public AzureNXTConnection(String target, AzConnectionType t) {
		super();
		this.target = target;
		this.type = t;
		reopenConnection();
		recv.start();
	}

	public AzureNXTConnection() {
		this(AzConnectionType.BLUETOOTH);
	}

	public AzureNXTConnection(AzConnectionType t) {
		this(null, t);
	}

	public static enum AzConnectionType {
		BLUETOOTH, RS485
	}

	@Override
	public String getPartnerName() {
		return target;
	}

	public void reopenConnection() {
		if (con != null) {
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (target == null) {
			if (type == AzConnectionType.BLUETOOTH) {
				con = Bluetooth.waitForConnection(0, NXTConnection.RAW);
			} else {
				con = RS485.waitForConnection(0, NXTConnection.RAW);
			}
			openStreams();
		} else {
			if (type == AzConnectionType.BLUETOOTH) {
				con = Bluetooth.connect(target, NXTConnection.RAW);
			} else {
				con = RS485.connect(target, NXTConnection.RAW);
			}
			openStreams();
		}

		if (con == null) {
			throw new RuntimeException("Failed to connect!");
		}
	}

	private void openStreams() {
		if (con != null) {
			out = con.openDataOutputStream();
			in = new AzInputStream(con.openInputStream());
			recv.bindInputStream(in);
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
