package azure.bt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import azure.bt.AzureConnection.ReceiverThread.PacketWrapper;
import azure.bt.packets.DefaultPacket;
import azure.common.AzInputStream;

public abstract class AzureConnection {
	protected ReceiverThread recv;

	public AzureConnection() {
		recv = new ReceiverThread(this);
	}

	public void addRecvListener(AzureConnectionRecvListener l) {
		recv.addRecvListener(l);
	}

	public abstract String getPartnerName();

	public abstract boolean getIsConnected();

	public int send(String str) throws IOException {
		DefaultPacket pkt = new DefaultPacket(str);

		return send(0, pkt);
	}

	public abstract int send(int type, AzPacket packet) throws IOException;

	public abstract void close() throws IOException;

	public void waitForMessage() {
		while (recv.data.isEmpty()) {
			Thread.yield();
		}
	}

	public String nextMessage() {
		PacketWrapper w = nextPacket();
		if (w.packetTypeId == 0) {
			DefaultPacket def = new DefaultPacket(w.dataAsStream());
			return def.getMessage();
		}
		return null;
	}

	public PacketWrapper nextPacket() {
		if (recv.data.isEmpty()) {
			return null;
		}
		return (PacketWrapper) recv.data.pop();
	}

	public long getNextMessageTime() {
		if (recv.data.isEmpty()) {
			return -1;
		}
		PacketWrapper w = (PacketWrapper) recv.data.peek();
		return w.timestamp;
	}

	public static class ReceiverThread extends Thread {
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

		public void bindInputStream(AzInputStream in) {
			invalidate();
			this.in = in;
		}

		public void invalidate() {
			in = null;
			data.clear();
		}

		@Override
		public void run() {
			try {
				while (!exited) {
					if (in == null) {
						continue;
					}
					int packetSize = in.readUnsignedShort();
					int packetFormat = in.read();
					long recvTime = System.currentTimeMillis();

					if (packetFormat != -1) {
						try {
							if (packetSize > Runtime.getRuntime().freeMemory()) {
								throw new OutOfMemoryError("PkSize: " + packetSize);
							}
							byte[] buf = new byte[packetSize];
							in.read(buf);
							PacketWrapper packet = PacketWrapper.wrap(packetFormat, buf);
							packet.timestamp = recvTime;
							if (!listeners.isEmpty()) {
								for (AzureConnectionRecvListener l : listeners) {
									l.onPacketReceived(packet);
								}
							} else {
								data.addElement(packet);
							}
						} catch (UnsupportedOperationException ex) {
							System.out.println("Sender: " + con.getPartnerName());
							System.out.println(ex.getMessage());
						}
					} else {
						exited = true;
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		public void shutdown() {
			exited = true;
		}

		public static class PacketWrapper {
			public long timestamp;

			public int packetTypeId;
			public byte[] packetData;

			public static PacketWrapper wrap(int typeId, byte[] data) {
				PacketWrapper w = new PacketWrapper();
				w.packetTypeId = typeId;
				w.packetData = data;
				return w;
			}

			public AzInputStream dataAsStream() {
				return new AzInputStream(new ByteArrayInputStream(packetData));
			}
		}
	}

	public static interface AzureConnectionRecvListener {
		public void onPacketReceived(PacketWrapper packet);
	}
}
