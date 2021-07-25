package azstudio;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import compression.LZ11;

/**
 * Converts a filesystem directory into an Azure-format archive.
 * 
 * PNG files are converted into native LeJOS platform bitmaps.
 * MIDI files are converted into AzureSeq's playback engine binaries.
 */
public class ArcConverter {

	public static final String AZURE_ARC_MAGIC = "AZUREARCHIVE";

	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[]{"azure_resources"};
		}
		File arcRoot = new File(args[0]);

		File ignore = new File(args[0] + ".ignore.txt");
		List<String> ignores = new ArrayList<>();
		if (ignore.exists()) {
			try {
				Scanner s = new Scanner(ignore);
				while (s.hasNextLine()) {
					ignores.add(s.nextLine());
				}
				s.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		List<ArcFileInfo> files = new ArrayList<>();

		addFileList(files, null, arcRoot);

		List<ArcFileInfo> toRemove = new ArrayList<>();
		for (ArcFileInfo i : files) {
			if (ignores.contains(i.resourceName)) {
				toRemove.add(i);
			}
		}
		files.removeAll(toRemove);

		// BEGIN FILE OUTPUT
		calculateOffsets(files);

		File output = new File(args[0] + ".arc");

		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(output));

			out.write(AZURE_ARC_MAGIC.getBytes());
			out.writeShort(files.size());
			for (ArcFileInfo i : files) {
				i.write(out, files);
			}
			for (ArcFileInfo i : files) {
				if (i.isDirectory) {
					continue;
				}
				out.write(i.origin);
			}
			out.close();

			LZ11.compressFile(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addFileList(List<ArcFileInfo> target, ArcFileInfo parent, File... files) {
		try {
			for (File f : files) {
				ArcFileInfo i = new ArcFileInfo();
				i.isDirectory = f.isDirectory();
				i.parentResource = parent;
				i.resourceName = f.getName();
				if (f.isDirectory()) {
					target.add(i);
					addFileList(target, i, f.listFiles());
				} else {
					if (f.getName().endsWith(".png")) {
						i.origin = ImageConverter.convertImage(f);
					} else {
						if (f.getName().endsWith(".mid")) {
							ArcFileInfo seqDir = new ArcFileInfo();
							seqDir.isDirectory = true;
							seqDir.parentResource = i.parentResource;
							seqDir.resourceName = i.resourceName;
							target.add(seqDir);

							byte[][] chdata = SoundSeqConverter.convertToMultiChannel(f);
							i.resourceName = "channel0.azseq";
							i.parentResource = seqDir;
							i.origin = chdata[0];
							for (int c = 1; c < chdata.length; c++) {
								ArcFileInfo newChannel = new ArcFileInfo();
								newChannel.isDirectory = false;
								newChannel.parentResource = seqDir;
								newChannel.resourceName = "channel" + c + ".azseq";
								newChannel.origin = chdata[c];
								newChannel.resourceDataLength = chdata[c].length;
								target.add(newChannel);
							}
						} else {
							i.origin = Files.readAllBytes(f.toPath());
						}
					}
					i.resourceDataLength = i.origin.length;
					target.add(i);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void calculateOffsets(List<ArcFileInfo> files) {
		int baseOfs = /* header length */ 0x0E;
		for (ArcFileInfo f : files) {
			baseOfs += f.getByteSize();
		}
		int ofs = baseOfs;
		for (ArcFileInfo f : files) {
			f.resourceDataOffset = ofs;
			ofs += f.resourceDataLength;
		}
	}

	public static class ArcFileInfo {

		public byte[] origin;

		public boolean isDirectory = false;
		public String resourceName;
		public ArcFileInfo parentResource;

		public int resourceDataOffset = -1;
		public int resourceDataLength = 0;

		public void write(DataOutputStream out, List<ArcFileInfo> library) throws IOException {
			out.write(isDirectory ? 1 : 0);
			StringUtils.writeString(out, resourceName);
			out.writeShort(library.indexOf(parentResource));
			out.writeShort(resourceDataOffset);
			out.writeShort(resourceDataLength);
		}

		public int getByteSize() {
			// 0x7 + string null terminator + resource name length
			return 0x8 + resourceName.length();
		}
	}
}
