package ch.tkuhn.hashuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.trig.TriGWriter;

import ch.tkuhn.nanopub.CustomTrigWriter;

public class TransformRdfFile {

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		String baseName = "";
		if (args.length > 1) {
			baseName = args[1];
		}
		RdfFileContent content = RdfUtils.load(inputFile);
		transform(content, inputFile.getParent(), baseName);
	}

	public static URI transform(RdfFileContent content, String outputDir, String baseName) throws Exception {
		URI baseURI = getBaseURI(baseName);
		String name = baseName;
		if (baseName.indexOf("/") > 0) {
			name = baseName.replaceFirst("^.*[^A-Za-z0-9.\\-_]([A-Za-z0-9.\\-_]*)$", "$1");
		}

		Map<String,Integer> blankNodeMap = new HashMap<>();
		String hash = makeHash(content, baseName, blankNodeMap);
		String fileName = name;
		if (fileName.length() == 0) {
			fileName = hash;
		} else {
			fileName += "." + hash;
		}
		OutputStream out = new FileOutputStream(new File(outputDir, fileName));
		TriGWriter writer = new CustomTrigWriter(out);
		HashAdder replacer = new HashAdder(baseURI, hash, writer, blankNodeMap);
		content.propagate(replacer);
		out.close();
		return RdfUtils.getHashURI(baseURI, baseURI, hash, blankNodeMap);
	}

	public static URI transform(InputStream in, OutputStream out, String baseName) throws Exception {
		URI baseURI = getBaseURI(baseName);
		RdfFileContent content = RdfUtils.load(in);
		Map<String,Integer> blankNodeMap = new HashMap<>();
		String hash = makeHash(content, baseName, blankNodeMap);
		TriGWriter writer = new CustomTrigWriter(out);
		HashAdder replacer = new HashAdder(baseURI, hash, writer, blankNodeMap);
		content.propagate(replacer);
		out.close();
		return RdfUtils.getHashURI(baseURI, baseURI, hash, blankNodeMap);
	}

	private static URI getBaseURI(String baseName) {
		URI baseURI = null;
		if (baseName.indexOf("/") > 0) {
			baseURI = new URIImpl(baseName);
		}
		return baseURI;
	}

	private static String makeHash(RdfFileContent content, String baseName, Map<String,Integer> blankNodeMap) {
		RdfHasher hasher = new RdfHasher(getBaseURI(baseName), blankNodeMap);
		return hasher.makeHash(content.getStatements());
	}

}
