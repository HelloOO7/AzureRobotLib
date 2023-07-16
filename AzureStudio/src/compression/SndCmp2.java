package compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SndCmp2 {

	public static void main(String[] args) {
		compressFile(new File("azure_resources/sdat/sample/BrassSect.wav"));
		//uncompressFile(new File("azure_resources/sdat/sample/BrassSect.wav.LZ"));
	}

	public static void uncompressFile(File f) {
		try {
			byte[] toUncompress = Files.readAllBytes(f.toPath());
			byte[] uncompressed = decompress(toUncompress);
			Files.write(Paths.get(f + ".raw"), uncompressed, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void compressFile(File f) {
		try {
			byte[] toCompress = Files.readAllBytes(f.toPath());
			byte[] compressed = compress(toCompress);
			Files.write(Paths.get(f + ".LZ"), compressed, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static byte[] decompress(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		return out.toByteArray();
	}


	public static byte[] compress(byte[] data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0x77);
		out.write(data.length & 0xFF);
		out.write((data.length & 0xFF00) >> 8);
		out.write((data.length & 0xFF0000) >> 16);

		for (int i = 0; i < data.length;) {
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			int qsearch;
			for (qsearch = i; qsearch < data.length; qsearch++) {
				int sample = data[qsearch] & 0xFF;
				if (sample < min) {
					min = sample;
				}
				if (sample > max) {
					max = sample;
				}
				if (max - min >= 32) {
					break;
				}
			}
			out.write(min);
			out.write(qsearch - i);

			for (int j = i; j < qsearch; j += 2) {
				int val1 = data[j] & 0xFF;
				int val2;
				if (j + 1 < data.length) {
					val2 = data[j + 1] & 0xFF;
				}
				else {
					val2 = 0;
				}
				out.write(
					(val1 >> 1) & 0xf
					| (((val2 >> 1) << 4) & 0xf0)
				);
			}

			i = qsearch;
		}

		return out.toByteArray();
	}
}
