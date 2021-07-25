package azstudio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageConverter {

	private static final String LNI0_MAGIC = "LNI0";
	private final static int WHITE = 0xffffffff;
	private final static int BLACK = 0;
	private static final int THRESHOLD_DEFAULT = 127;

	public static byte[] convertImage(File source) throws IOException{
		BufferedImage img = ImageIO.read(source);
		BufferedImage bw = removeColor(img, THRESHOLD_DEFAULT);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		out.write(LNI0_MAGIC.getBytes("ASCII"));
		dos.writeShort(bw.getWidth());
		dos.writeShort(bw.getHeight());
		out.write(nxtImageConvert(bw));
		return out.toByteArray();
	}

	public static BufferedImage removeColor(BufferedImage colorImage, int threshold) {
		int w = colorImage.getWidth();
		int h = colorImage.getHeight();
		int[] argbs = colorImage.getRGB(0, 0, w, h, null, 0, w);
		int[] bws = new int[argbs.length];
		for (int i = 0; i < argbs.length; i++) {
			bws[i] = getH(argbs[i]) > threshold ? WHITE : BLACK;
		}
		BufferedImage image = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY);
		image.setRGB(0, 0, w, h, bws, 0, w);
		return image;
	}

	public static byte[] nxtImageConvert(BufferedImage image) {
		if (image == null || image.getType() != BufferedImage.TYPE_BYTE_BINARY)
			throw new IllegalArgumentException();

		int w = image.getWidth();
		int h = image.getHeight();
		int n = h >> 3;
		if (h > n << 3) {
			n++;
		}

		byte[] data = new byte[n * w];
		int index = 0;
		for (int i = 0; i < h; i += 8) {
			for (int j = 0; j < w; j++) {
				byte d = 0;
				for (int k = 7; k >= 0; k--) {
					d <<= 1;
					int x = j;
					int y = i + k;
					if (y < h) {
						int argb = image.getRGB(x, y);
						d |= (byte) ((argb & 0xffffff) > 0 ? 0 : 1);
					}
				}
				data[index++] = d;
			}
		}

		return data;
	}

	private static int getH(int argb) {
		int b = (argb & 0xff);
		int g = ((argb >>> 8) & 0xff);
		int r = ((argb >>> 16) & 0xff);
		int h = (r << 14) + (r << 12) + (g << 15) + (g << 12) + (b << 13);
		return h >> 16;
	}
}
