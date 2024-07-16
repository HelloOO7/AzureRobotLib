package azstudio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import azstudio.ArcConverter.ArcFileInfo;

public class ArcExtractor {
	public static void main(String[] args) {
		String arcName = "azure_resources.arc";
		if (args.length > 0) {
			arcName = args[0];
		}
		try {
			String outRoot = new File(arcName).getAbsoluteFile().getParent() + "/" + arcName + "_ext";

			RandomAccessFile raf = new RandomAccessFile(arcName, "r");
			AzInputStream in = new AzInputStream(new FileInputStream(arcName));

			in.getMagic(ArcConverter.AZURE_ARC_MAGIC);
			int fileCount = in.readUnsignedShort();
			List<ArcFileInfo> tree = new ArrayList<>();
			for (int i = 0; i < fileCount; i++) {
				ArcFileInfo info = ArcFileInfo.read(in, tree);
				if (info.isDirectory) {
					File dir = new File(outRoot + info.getFullResPath());
					dir.mkdirs();
				}
				else {
					raf.seek(info.resourceDataOffset);
					byte[] data = new byte[info.resourceDataLength];
					raf.read(data);
					Files.write(Paths.get(outRoot + info.getFullResPath()), data, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				}
				tree.add(info);
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
