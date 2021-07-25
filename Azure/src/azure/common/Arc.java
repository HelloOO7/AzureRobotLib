package azure.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

	public byte[] getFile(String resPath) {
		try {
			ArcFileInfo i = getFileDescriptor(files.get(0).getFullResourceName() + resPath);
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

	public InputStream getFileAsStream(String resPath){
		try {
			FileInputStream in = new FileInputStream(origin);
			in.skip(getFileDescriptor(files.get(0).getFullResourceName() + resPath).resourceDataOffset);
			return in;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ArcFileInfo getFileDescriptor(String resPath) {
		for (ArcFileInfo i : files) {
			if (i.getFullResourceName().equals(resPath)) {
				return i;
			}
		}
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
