package net.trustyuri.file;

import java.io.File;
import java.io.FileInputStream;

public class ProcessFile {

	public static void main(String[] args) throws Exception {
		String filename = args[0];
		File file = new File(filename);
		FileHasher hasher = new FileHasher();
		String ac = hasher.makeArtifactCode(new FileInputStream(file));
		String ext = "";
		String base = filename;
		if (filename.matches(".+\\.[A-Za-z0-9\\-_]{0,20}")) {
			ext = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$2");
			base = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$1");
		}
		File hashFile = new File(base + "." + ac + ext);
		file.renameTo(hashFile);
	}

}
