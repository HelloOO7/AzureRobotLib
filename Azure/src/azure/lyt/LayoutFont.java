package azure.lyt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import azure.common.AzInputStream;

public class LayoutFont {

	public static final String AZURE_FONT_MAGIC = "AFNT";

	private byte[] buf;
	public byte fontSize;
	private byte charaOffset;
	private byte charaCount;
	public byte glyphWidth;

	public LayoutFont(byte[] source){
		try {
			AzInputStream in = new AzInputStream(new ByteArrayInputStream(source));
			if (!in.getMagic(AZURE_FONT_MAGIC)){
				in.close();
				throw new IllegalArgumentException("Source is not an Azure font file.");
			}
			fontSize = in.readByte();
			glyphWidth = in.readByte();
			charaOffset = in.readByte();
			charaCount = in.readByte();
			buf = new byte[in.available()];
			in.read(buf);

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] getCharacterRaster(char c){
		if (c - charaOffset >= charaCount){
			throw new IllegalArgumentException("Character is outside of the font's boundaries.");
		}
		int charaIndex = glyphWidth * (c - charaOffset);
		return Arrays.copyOfRange(buf, charaIndex, charaIndex + glyphWidth);
	}
}
