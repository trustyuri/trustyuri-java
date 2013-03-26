package ch.tkuhn.hashuri.file;

import java.io.File;
import java.io.FileInputStream;

public class ProcessFile {

	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		FileHasher hasher = new FileHasher();
		String hash = hasher.makeHash(new FileInputStream(file));
		File hashFile = new File(args[0] + "." + hash);
		file.renameTo(hashFile);
	}

}
