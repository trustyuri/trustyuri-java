package ch.tkuhn.hashrdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.trig.TriGWriter;

public class TransformFile {

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		String baseName = "";
		if (args.length > 1) {
			baseName = args[1];
		}
		RDFFileContent content = FileUtils.load(inputFile);
		transform(content, inputFile.getParent(), baseName);
	}

	public static void transform(RDFFileContent content, String outputDir, String baseName) throws Exception {
		String name = baseName;
		URI baseURI = null;
		if (baseName.indexOf("/") > 0) {
			baseURI = new URIImpl(baseName);
			name = baseName.replaceFirst("^.*[^A-Za-z0-9.\\-_]([A-Za-z0-9.\\-_]*)$", "$1");
		}

		Map<String,Integer> blankNodeMap = new HashMap<>();
		Hasher hasher = new Hasher(baseURI, blankNodeMap);
		String hash = hasher.makeHash(content.getStatements());
		String fileName = name;
		if (fileName.length() == 0) {
			fileName = hash;
		} else {
			fileName += "." + hash;
		}
		File outputFile = new File(outputDir, fileName);
		OutputStream out = new FileOutputStream(outputFile);
		TriGWriter writer = new CustomTriGWriter(out);
		HashAdder replacer = new HashAdder(baseURI, hash, writer, blankNodeMap);
		content.propagate(replacer);
		out.close();
	}

}
