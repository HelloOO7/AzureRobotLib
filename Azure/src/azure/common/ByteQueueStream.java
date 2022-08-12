package azure.common;

import java.io.IOException;
import java.io.InputStream;

public class ByteQueueStream extends InputStream {
	private static final int INITIAL_CAPACITY = 256;
	private static final int CAPACITY_INCREMENT_NUM = 3;
	private static final int CAPACITY_INCREMENT_DEN = 2;

	protected byte[] buf;
	protected int readPos = 0;
	protected int writePos = 0;

	public ByteQueueStream() {
		buf = new byte[INITIAL_CAPACITY];
	}

	public void ensureCapacity(int minCapacity) {
		synchronized (this) {
			int cl = buf.length;
			if (cl < minCapacity) {
				cl = cl * CAPACITY_INCREMENT_NUM / CAPACITY_INCREMENT_DEN + 1;
				while (cl < minCapacity)
					cl = cl * CAPACITY_INCREMENT_NUM / CAPACITY_INCREMENT_DEN + 1;

				byte[] newData = new byte[cl];
				System.arraycopy(buf, 0, newData, 0, writePos);
				buf = newData;
			}
		}
	}

	public void shift() {
		synchronized (this) {
			if (readPos >= (buf.length >> 1)) {
				System.arraycopy(buf, readPos, buf, 0, writePos - readPos);
				writePos -= readPos;
				readPos = 0;
			}
		}
	}

	public void write(byte[] b, int off, int len) {
		this.ensureCapacity(this.writePos + len);
		System.arraycopy(b, off, this.buf, this.writePos, len);
		this.writePos += len;
	}

	/*public void write(byte[] b, int off, int len) {
		for (int i = off; i < off + len; i++) {
			write(b[i]);
		}
	}*/

	public void write(int b) {
		//System.out.println("write " + b);
		this.ensureCapacity(this.writePos + 1);
		this.buf[this.writePos] = (byte) b;
		this.writePos++;
	}

	@Override
	public int read() throws IOException {
		while (readPos >= writePos) {
			Thread.yield();
		}
		synchronized (this) {
			int r = buf[readPos] & 0xFF;
			//System.out.println("read " + r);
			readPos++;
			shift();
			return r;
		}
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		//System.out.println("block read");
		int needWritePos = readPos + len;
		while (writePos < needWritePos) {
			Thread.yield();
		}
		synchronized (this) {
			System.arraycopy(buf, readPos, b, off, len);
			readPos += len;
			shift();
			return len;
		}
	}

	@Override
	public long skip(long n) {
		long skipped = Math.min(n, writePos - readPos);
		if (skipped < 0) {
			skipped = 0;
		} else {
			readPos += skipped;
		}
		return skipped;
	}
}
