package compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SndCmp {

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

		int magic = in.read();
		if (magic == 0x77) {
			int len = in.read() | (in.read() << 8) | (in.read() << 16);
			int outPos = 1;
			int lastValue = in.read();
			out.write(lastValue);
			while (outPos < len) {
				int value = in.read();
				CompressionMode mode = CompressionMode.values()[value >>> 6];
				value &= 0x3F;
				int sample = lastValue;
				switch (mode) {
				case SIGNED:
					sample = signExtend6Bit(value) + lastValue;
					out.write(sample);
					outPos++;
					break;
				case LARGE:
					int large = in.read();
					sample = value | (large & (0b11 << 6));
					out.write(sample);
					outPos++;
					if ((large & (1 << 5)) != 0) {
						sample = signExtend5Bit(large) + sample;
						out.write(sample);
						outPos++;
					}
					break;
				case ADD2:
				case SUB2:
					int diff1 = value & 0x7;
					int diff2 = (value >> 3) & 0x7;
					if (mode == CompressionMode.ADD2) {
						sample = lastValue + diff1;
					}
					else {
						sample = lastValue - diff1;
					}
					out.write(sample);
					outPos++;
					if (outPos < len) {
						lastValue = sample;
						if (mode == CompressionMode.ADD2) {
							sample = lastValue + diff2;
						}
						else {
							sample = lastValue - diff2;
						}
						out.write(sample);
						outPos++;
					}
					break;
				}
				lastValue = sample;
				//System.out.println("Mode " + mode + " @ 0x" + Integer.toHexString(data.length - in.available()) + " sample 0x" + Integer.toHexString(sample));
			}
		}
		else {
			throw new RuntimeException("Invalid magic");
		}

		return out.toByteArray();
	}


	public static byte[] compress(byte[] data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0x77);
		out.write(data.length & 0xFF);
		out.write((data.length & 0xFF00) >> 8);
		out.write((data.length & 0xFF0000) >> 16);

		int lastVal = data[0] & 0xFF;
		out.write(lastVal);
		boolean isVal2Invalid = false;
		for (int i = 0; i < data.length;) {
			int val1 = data[i] & 0xFF;
			int val2;
			if (i + 1 < data.length) {
				val2 = data[i + 1] & 0xFF;
				isVal2Invalid = false;
			}
			else {
				val2 = 0;
				isVal2Invalid = true;
			}

			int diff1 = val1 - lastVal;
			int diff2 = val2 - val1;
			int absd1 = Math.abs(diff1);
			int absd2 = Math.abs(diff2);
			int signd1 = (int)Math.signum(diff1);
			int signd2 = (int)Math.signum(diff2);
			CompressionMode mode = null;
			if (!isVal2Invalid
					&& (absd1 < 0x8 && absd2 < 0x8)
					&& (signd1 == 0 || signd2 == 0 || (signd1 == signd2))) {
				if (signd1 == 1 || signd2 == 1) {
					//Either both add, or one add, another zero (don't care)
					mode = CompressionMode.ADD2;
				}
				else {
					mode = CompressionMode.SUB2;
				}

				out.write((mode.ordinal() << 6) | (absd1 & 0x7) | ((absd2 & 0x7) << 3));
				lastVal = val2;
				i += 2;
			} else if (Math.abs(diff1) < 0x20) {
				mode = CompressionMode.SIGNED;
				out.write((mode.ordinal() << 6) | signShrink6Bit(diff1));
				lastVal = val1;
				i++;
			} else {
				mode = CompressionMode.LARGE;
				int byte1 = mode.ordinal() << 6;
				byte1 |= val1 & 0b111111;
				int byte2 = (val1 & (0b11 << 6));
				if (absd2 < 0x10 && !isVal2Invalid) {
					byte2 |= (1 << 5);
					byte2 |= signShrink5Bit(diff2);
					lastVal = val2;
					i += 2;
				}
				else {
					lastVal = val1;
					i++;
				}
				out.write(byte1);
				out.write(byte2);
			}
			System.out.println("Compressed values " + Integer.toHexString(val1) + ", " + Integer.toHexString(val2) + " using " + mode + " @ " + Integer.toHexString(out.size()));
		}

		return out.toByteArray();
	}

	private static int signShrink5Bit(int val) {
		return (val << 27) >>> 27;
	}

	private static int signShrink6Bit(int val) {
		return (val << 26) >>> 26;
	}

	private static int signExtend5Bit(int val) {
		return (val << 27) >> 27;
	}

	private static int signExtend6Bit(int val) {
		return (val << 26) >> 26;
	}

	private static enum CompressionMode {
		ADD2, //00
		SUB2, //01
		SIGNED, //10
		LARGE, //11
		ADD_SUB,
		SUB_ADD
	}
}
