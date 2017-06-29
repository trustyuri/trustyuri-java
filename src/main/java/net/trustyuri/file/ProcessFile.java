package net.trustyuri.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ProcessFile {

	public static void main(String[] args) throws IOException {
		String filename = args[0];
		File file = new File(filename);
		process(file);
	}

	public static void process(File file) throws IOException {
		String filename = file.getName();
		FileHasher hasher = new FileHasher();
		String ac = hasher.makeArtifactCode(new BufferedInputStream(new FileInputStream(file)));
		String ext = "";
		String base = filename;
		if (filename.matches(".+\\.[A-Za-z0-9\\-_]{0,20}")) {
			ext = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$2");
			base = filename.replaceFirst("^(.*)(\\.[A-Za-z0-9\\-_]{0,20})$", "$1");
		}
		String glue = "";
		if (base.length() > 0 && base.charAt(base.length()-1) != '.') {
			glue = ".";
		}
		File hashFile = new File(file.getParentFile(), base + glue + ac + ext);
		file.renameTo(hashFile);
	}

}
