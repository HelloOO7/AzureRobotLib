package azure.bt;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import azure.bt.AzureConnection.ReceiverThread.PacketWrapper;
import azure.bt.packets.DefaultPacket;
import azure.common.AzInputStream;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class AzureConnection {
	private final String target;

	private BTConnection btCon;

	private DataOutputStream out;
	private AzInputStream in;

	private ReceiverThread recv = new ReceiverThread();

	public AzureConnection(String target){
		this.target = target;
		reopenConnection();
		recv.start();
	}

	public AzureConnection(){
		target = null;
		reopenConnection();
		recv.start();
	}

	public void reopenConnection() {
		if (btCon != null) {
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (target == null) {
			btCon = Bluetooth.waitForConnection(0, NXTConnection.RAW);
			openStreams();
		}
		else {
			btCon = Bluetooth.connect(target, NXTConnection.RAW);
			openStreams();
		}
	}

	public void addRecvListener(AzureConnectionRecvListener l) {
		recv.addRecvListener(l);
	}

	private void openStreams(){
		if (btCon != null){
			out = btCon.openDataOutputStream();
			in = new AzInputStream(btCon.openInputStream());
			recv.bindInputStream(in);
		}
	}

	public int getSignal(){
		return btCon.getSignalStrength();
	}

	public boolean getIsConnected(){
		return btCon != null;
	}

	public void send(String str) throws IOException{
		out.write(0);
		new DefaultPacket(str).send(out);
		out.flush();
	}

	public void close() throws IOException {
		out.close();
		btCon.close();
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

	public static class ReceiverThread extends Thread{
		private boolean exited = false;
		private AzInputStream in = null;

		public Queue<PacketWrapper> data = new Queue<>();

		private List<AzureConnectionRecvListener> listeners = new ArrayList<>();

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
					switch (packetFormat){
						case 0:
							PacketWrapper packet = PacketWrapper.wrap(
									DefaultPacket.class,
									new DefaultPacket(in)
							);
							data.addElement(packet);
							for (AzureConnectionRecvListener l : listeners) {
								l.onPacketReceived(packet);
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
