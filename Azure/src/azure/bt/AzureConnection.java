package azure.bt;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import azure.bt.AzureConnection.ReceiverThread.PacketWrapper;
import azure.bt.packets.DefaultPacket;
import azure.common.AzInputStream;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RS485;

public class AzureConnection {
	private final String target;

	private NXTConnection con;

	private DataOutputStream out;
	private AzInputStream in;

	private ReceiverThread recv;

	private AzConnectionType type;

	public AzureConnection(String target){
		this(target, AzConnectionType.BLUETOOTH);
	}

	public AzureConnection(String target, AzConnectionType t){
		this.target = target;
		this.type = t;
		recv = new ReceiverThread(this);
		reopenConnection();
		recv.start();
	}

	public AzureConnection() {
		this(AzConnectionType.BLUETOOTH);
	}

	public AzureConnection(AzConnectionType t){
		target = null;
		this.type = t;
		recv = new ReceiverThread(this);
		reopenConnection();
		recv.start();
	}

	public static enum AzConnectionType {
		BLUETOOTH,
		RS485
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
			}
			else {
				con = RS485.waitForConnection(0, NXTConnection.RAW);
			}
			openStreams();
		}
		else {
			if (type == AzConnectionType.BLUETOOTH) {
				con = Bluetooth.connect(target, NXTConnection.RAW);
			}
			else {
				con = RS485.connect(target, NXTConnection.RAW);
			}
			openStreams();
		}

		if (con == null) {
			throw new RuntimeException("Failed to connect!");
		}
	}

	public void addRecvListener(AzureConnectionRecvListener l) {
		recv.addRecvListener(l);
	}

	private void openStreams(){
		if (con != null){
			out = con.openDataOutputStream();
			in = new AzInputStream(con.openInputStream());
			recv.bindInputStream(in);
		}
	}

	public boolean getIsConnected(){
		return con != null;
	}

	public int send(String str) throws IOException{
		DefaultPacket pkt = new DefaultPacket(str);

		out.flush();
		out.write(0);
		pkt.send(out);
		long start = System.currentTimeMillis();
		out.flush();

		return (int)(System.currentTimeMillis() - start);
	}

	public void close() throws IOException {
		con.close();
	}

	public void waitForMessage(){
		while (recv.data.isEmpty()){
			Thread.yield();
		}
	}

	public String nextMessage(){
		if (recv.data.isEmpty()){
			return null;
		}
		PacketWrapper w = (PacketWrapper) recv.data.pop();
		if (w.packetClass == DefaultPacket.class){
			return ((DefaultPacket)w.packet).getMessage();
		}
		return null;
	}

	public long getNextMessageTime() {
		if (recv.data.isEmpty()){
			return -1;
		}
		PacketWrapper w = (PacketWrapper) recv.data.peek();
		return w.timestamp;
	}

	public static class ReceiverThread extends Thread{
		private boolean exited = false;
		private AzInputStream in = null;
		private AzureConnection con;

		public Queue<PacketWrapper> data = new Queue<>();

		private List<AzureConnectionRecvListener> listeners = new ArrayList<>();

		public ReceiverThread(AzureConnection con) {
			this.con = con;
		}

		public void addRecvListener(AzureConnectionRecvListener l) {
			if (l != null && !listeners.contains(l)) {
				listeners.add(l);
			}
		}

		public void bindInputStream(AzInputStream in){
			invalidate();
			this.in = in;
		}

		public void invalidate(){
			in = null;
			data.clear();
		}

		@Override
		public void run(){
			try {
				while (!exited){
					if (in == null){
						continue;
					}
					int packetFormat = in.read();
					long recvTime = System.currentTimeMillis();
					switch (packetFormat){
						case 0:
							try {
							PacketWrapper packet = PacketWrapper.wrap(
									DefaultPacket.class,
									new DefaultPacket(in)
							);
							packet.timestamp = recvTime;
							data.addElement(packet);
							for (AzureConnectionRecvListener l : listeners) {
								l.onPacketReceived(packet);
							}
							} catch (UnsupportedOperationException ex) {
								System.out.println("Sender: " + con.target);
								System.out.println(ex.getMessage());
							}
							break;
						case -1:
							exited = true;
							break;
						default:
							throw new IllegalArgumentException(packetFormat + ": Invalid packet class.");
					}
				}
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}

		public void shutdown(){
			exited = true;
		}

		public static class PacketWrapper{
			public long timestamp;

			public Class<? extends AzPacket> packetClass;
			public AzPacket packet;

			public static PacketWrapper wrap(Class<? extends AzPacket> pc, AzPacket p){
				PacketWrapper w = new PacketWrapper();
				w.packetClass = pc;
				w.packet = p;
				return w;
			}
		}
	}

	public static interface AzureConnectionRecvListener {
		public void onPacketReceived(PacketWrapper packet);
	}
}
