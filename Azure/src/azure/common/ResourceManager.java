package azure.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.microedition.lcdui.Image;

import azure.common.Arc.ArcFileInfo;
import azure.lyt.LayoutFont;
import azure.util.Native;
import azure.util.NativeFlash;
import lejos.nxt.Flash;

public class ResourceManager {

	private static final String AZURE_DEFAULT_RESOURCE = "azure_resources.arc";

	private static Map<String, File> mountedArcs = new HashMap<>();
	private static Map<String, Resource> resources = new HashMap<>();

	private static LayoutFont defaultLayoutFont;

	static {
		init();
	}

	public static LayoutFont getDefaultFont() {
		return defaultLayoutFont;
	}

	public static boolean isArchiveReady(String archivePath) {
		return mountedArcs.containsKey(archivePath);
	}

	public static void mountArchive(String archivePath) {
		if (!isArchiveReady(archivePath)) {
			File file = new File(archivePath);
			if (file.exists()) {
				Arc arc = new Arc(file);
				File baseFile = arc.getFlashFile();
				for (ArcFileInfo fi : arc.files) {
					resources.put(fi.getFullResourceName(), new Resource(baseFile, fi.resourceDataOffset, fi.resourceDataLength));
				}
				mountedArcs.put(archivePath, baseFile);
				arc = null;
				System.gc();
			} else {
				throw new RuntimeException("AZ_RSC: Could not load resource archive!");
			}
		}
	}

	public static Resource getResource(String resName) {
		Resource res = resources.get(resName);
		if (res != null) {
			return res;
		}
		throw new RuntimeException(resName + " not found!");
	}

	public static void init() {
		mountArchive(AZURE_DEFAULT_RESOURCE);
		defaultLayoutFont = new LayoutFont(getResource("/AzLytStd.azfnt").openStream());
	}

	public static Image readImage(String resource) {
		try {
			AzInputStream imageData = new AzInputStream(getResource(resource).openStream());
			imageData.getMagic("LNI0");
			short width = imageData.readShort();
			short height = imageData.readShort();
			byte[] data = imageData.readSizedArray(imageData.available());
			imageData.close();
			return new Image(width, height, data);
		} catch (IOException e) {
			System.err.println("Could not read image " + resource);
			e.printStackTrace();
			return null;
		}
	}

	public static class Resource {
		public final File file;
		private final int page;
		public final int offset;
		public final int length;

		public Resource(File file) {
			this(file, 0, (int)file.length());
		}

		public Resource(File file, int offset, int length) {
			this.file = file;
			this.offset = offset;
			this.length = length;
			page = file.getPage();
		}

		public int getPage() {
			return getPage(0);
		}

		public int getPage(int increment) {
			int base = page;
			int offs = offset + increment;
			if (offs >= Flash.BYTES_PER_PAGE) {
				base += offs / Flash.BYTES_PER_PAGE;
			}
			return base;
		}

		public int getOffsetInPage() {
			return getOffsetInPage(0);
		}

		public int getOffsetInPage(int increment) {
			return (offset + increment) & Flash.BYTES_PER_PAGE;
		}

		public int getMemoryAddress() {
			return NativeFlash.getFlashRawAddress(page, offset);
		}

		public InputStream openStream() {
			try {
				FileInputStreamEx stream;
				stream = new FileInputStreamEx(file);
				stream.seek(offset);
				return stream;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		public byte[] getBytes() {
			byte[] data = new byte[length];
			//Directly reading from flash memory via C memcopy
			Native.memCopy(data, 0, Native.ABSOLUTE, getMemoryAddress(), length);
			return data;
		}
	}
}
