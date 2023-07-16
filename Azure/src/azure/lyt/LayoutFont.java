package azure.lyt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.microedition.lcdui.Image;

import azure.common.AzInputStream;

public class LayoutFont {

	public static final String AZURE_FONT_MAGIC = "AFNT";

	public byte[] buf;
	public int fontSize;
	public int charaOffset;
	public int charaCount;
	public int glyphWidth;
	public int cellWidth;

	public LayoutFont(InputStream stream){
		try {
			AzInputStream in = new AzInputStream(stream);
			if (!in.getMagic(AZURE_FONT_MAGIC)){
				in.close();
				throw new IllegalArgumentException("Source is not an Azure font file.");
			}
			fontSize = in.readByte();
			glyphWidth = in.readByte();
			cellWidth = in.readByte();
			charaOffset = in.readByte();
			charaCount = in.readByte();
			buf = new byte[in.available()];
			in.read(buf);

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LayoutFont(Image image, int glyphWidth, int cellWidth, int charOffset, int charCount) {
		buf = image.getData();
		fontSize = image.getHeight();
		this.glyphWidth = glyphWidth;
		this.cellWidth = cellWidth;
		this.charaOffset = charOffset;
		this.charaCount = charCount;
	}

	private int div8Ceil(int val) {
		return (val + 7) >> 3;
	}

	public byte[] getCharacterRaster(char c){
		if (c - charaOffset >= charaCount){
			throw new IllegalArgumentException("Character is outside of the font's boundaries.");
		}
		int charaIndex = div8Ceil(glyphWidth * (c - charaOffset) * fontSize);
		return Arrays.copyOfRange(buf, charaIndex, charaIndex + div8Ceil(glyphWidth * fontSize));
	}
}
