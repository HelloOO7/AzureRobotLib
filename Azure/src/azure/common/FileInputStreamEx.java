package azure.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import lejos.nxt.Flash;

/**
 * Reads a stream of bytes from a file. This stream uses an internal Buffer of
 * 256 bytes.
 *
 * @author Brian Bagnall
 * @author Sven Köhler
 */
public class FileInputStreamEx extends InputStream {
	private int offset;
	private int file_limit;
	private int page_low;
	private int page_high;
	private int first_page;
	private byte[] buff;

	public FileInputStreamEx(File f) throws FileNotFoundException {
		if (!f.exists())
			throw new FileNotFoundException(f + " does not exist");

		this.buff = new byte[Flash.BYTES_PER_PAGE];

		this.offset = 0;
		this.file_limit = (int) f.length();
		this.page_low = 0;
		this.page_high = 0;
		this.first_page = f.getPage();
	}

	@Override
	public int available() throws IOException {
		return this.file_limit - this.offset;
	}

	private void buffPage() {
		if (this.offset < page_high && this.offset >= page_low)
			return;

		int pnum = this.offset / Flash.BYTES_PER_PAGE;
		Flash.readPage(this.buff, this.first_page + pnum);
		this.page_low = pnum * Flash.BYTES_PER_PAGE;
		this.page_high = this.page_low + Flash.BYTES_PER_PAGE;
	}
	
	public void setFileLimit(int limit) {
		file_limit = limit;
	}

	@Override
	public int read() throws IOException {
		if (this.offset >= this.file_limit)
			return -1;

		this.buffPage();

		return buff[this.offset++ % Flash.BYTES_PER_PAGE] & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (this.offset >= this.file_limit)
			return -1;

		int favail = this.file_limit - this.offset;
		if (len > favail)
			len = favail;

		int offorig = off;
		while (len > 0) {
			this.buffPage();

			int pavail = this.page_high - this.offset;
			if (pavail > len)
				pavail = len;

			System.arraycopy(this.buff, this.offset % Flash.BYTES_PER_PAGE, b, off, pavail);
			this.offset += pavail;
			off += pavail;
			len -= pavail;
		}
		return off - offorig;
	}

	public int getPosition() {
		return offset;
	}

	public boolean seek(long pos) {
		if (pos >= 0 && pos < file_limit) {
			offset = (int)pos;
			return true;
		}
		return false;
	}

	@Override
	public long skip(long n) throws IOException {
		if (n <= 0)
			return 0;

		int avail = this.file_limit - this.offset;
		if (avail > n)
			avail = (int) n;

		this.offset += avail;

		return n - avail;
	}
}