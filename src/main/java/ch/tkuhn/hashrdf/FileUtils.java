package ch.tkuhn.hashrdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.openrdf.rio.trig.TriGParser;

public class FileUtils {

	private FileUtils() {}  // no instances allowed

	public static RDFFileContent loadFile(File file) throws Exception {
		InputStream in = new FileInputStream(file);
		TriGParser p = new TriGParser();
		RDFFileContent content = new RDFFileContent();
		p.setRDFHandler(content);
		p.parse(in, "");
		in.close();
		return content;
	}

	public static String getHashPart(String s) {
		if (!s.matches("(.*[^A-Za-z0-9\\-_]|)[A-Za-z0-9\\-_]{43}")) {
			return null;
		}
		return s.replaceFirst("^(.*[^A-Za-z0-9\\-_]|)([A-Za-z0-9\\-_]{43})$", "$2");
	}

}
