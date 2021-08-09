package azure.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lejos.nxt.Button;
import lejos.nxt.Flash;

public class Arc {
	private File origin;

	public static final String AZURE_ARC_MAGIC = "AZUREARCHIVE";

	public List<ArcFileInfo> files = new ArrayList<>();

	public Arc(File f) {
		origin = f;
		try {
			AzInputStream in = new AzInputStream(new FileInputStream(f));
			if (!in.getMagic(AZURE_ARC_MAGIC)) {
				in.close();
				throw new IllegalArgumentException("AZ_ARC: Invalid magic.");
			}
			short len = in.readShort();
			for (int i = 0; i < len; i++) {
				files.add(ArcFileInfo.read(in, files));
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPageForOffset(int offset) {
		return origin.getPage() + offset / Flash.BYTES_PER_PAGE;
	}

	public int getOffsetInPage(int offset) {
		return offset % Flash.BYTES_PER_PAGE;
	}

	public byte[] getFile(String resPath) {
		try {
			ArcFileInfo i = getFileDescriptor(resPath);
			if (i != null && !i.isDirectory) {
				// Open origin input stream and retrieve the file from the
				// cached offset
				AzInputStream in = new AzInputStream(new FileInputStream(origin));
				in.skip(i.resourceDataOffset);
				byte[] buf = new byte[i.resourceDataLength];
				in.read(buf);
				in.close();
				return buf;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("AZ_ARC: This archive does not contain the given resource.");
	}

	public InputStream getFileAsStream(ArcFileInfo info) throws IOException {
		FileInputStream in = new FileInputStream(origin);
		in.skip(info.resourceDataOffset);
		return in;
	}

	public InputStream getFileAsStream(String resPath){
		try {
			return getFileAsStream(getFileDescriptor(resPath));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ArcFileInfo getFileDescriptor(String resPath) {
		resPath = files.get(0).getFullResourceName() + resPath;
		for (ArcFileInfo i : files) {
			if (i.getFullResourceName().equals(resPath)) {
				return i;
			}
		}
		System.err.println("Resource " + resPath + " not found!");
		Button.waitForAnyPress();
		System.exit(0);
		return null;
	}

	public static class ArcFileInfo {
		public boolean isDirectory = false;
		public String resourceName;
		public ArcFileInfo parentResource;

		public int resourceDataOffset;
		public int resourceDataLength;

		public static ArcFileInfo read(AzInputStream dis, List<ArcFileInfo> tree) throws IOException {
			ArcFileInfo i = new ArcFileInfo();
			i.isDirectory = dis.read() == 1;
			i.resourceName = dis.readString();
			int pidx = dis.readShort();
			if (pidx != -1){
				i.parentResource = tree.get(pidx);
			}
			i.resourceDataOffset = dis.readUnsignedShort();
			i.resourceDataLength = dis.readUnsignedShort();
			return i;
		}

		public String getFullResourceName() {
			StringBuilder sb = new StringBuilder();
			if (parentResource != null) {
				sb.append(parentResource.getFullResourceName());
			}
			sb.append("/");
			sb.append(resourceName);
			return sb.toString();
		}
	}
}
