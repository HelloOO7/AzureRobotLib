package azure.common;

import java.io.File;
import java.io.IOException;

import javax.microedition.lcdui.Image;

import azure.lyt.LayoutFont;

public class ResourceManager {

	private static final String AZURE_DEFAULT_RESOURCE = "azure_resources.arc";

	public static Arc lyArc;

	private static LayoutFont font;

	static {
		init();
	}

	public static void init() {
		if (lyArc == null) {
			File defaultResources = new File(AZURE_DEFAULT_RESOURCE);
			if (defaultResources.exists()) {
				lyArc = new Arc(defaultResources);
				font = new LayoutFont(lyArc.getFile("/AzLytStd.azfnt"));
			} else {
				throw new RuntimeException("AZ_RSC: Could not load common resource archive!");
			}
		}
	}

	public static LayoutFont getDefaultFont() {
		return font;
	}

	public static Image readImage(String resource) {
		try {
			AzInputStream imageData = new AzInputStream(lyArc.getFileAsStream(resource));
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
}
