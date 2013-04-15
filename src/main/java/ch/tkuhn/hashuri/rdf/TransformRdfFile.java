package ch.tkuhn.hashuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;

import ch.tkuhn.hashuri.HashUriResource;
import ch.tkuhn.nanopub.CustomTrigWriterFactory;

public class TransformRdfFile {

	static {
		RDFWriterRegistry.getInstance().add(new CustomTrigWriterFactory());
	}

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		String baseName = "";
		if (args.length > 1) {
			baseName = args[1];
		}
		RdfFileContent content = RdfUtils.load(new HashUriResource(inputFile));
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
		RDFFormat format = content.getOriginalFormat();
		String ext = "";
		if (!format.getFileExtensions().isEmpty()) {
			ext = "." + format.getFileExtensions().get(0);
		}
		if (fileName.length() == 0) {
			fileName = hash + ext;
		} else {
			fileName += "." + hash + ext;
		}
		OutputStream out = new FileOutputStream(new File(outputDir, fileName));
		RDFWriter writer = Rio.createWriter(format, out);
		HashAdder replacer = new HashAdder(baseURI, hash, writer, blankNodeMap);
		content.propagate(replacer);
		out.close();
		return RdfUtils.getHashURI(baseURI, baseURI, hash, blankNodeMap);
	}

	public static URI transform(InputStream in, RDFFormat format, OutputStream out, String baseName) throws Exception {
		URI baseURI = getBaseURI(baseName);
		RdfFileContent content = RdfUtils.load(in, format);
		Map<String,Integer> blankNodeMap = new HashMap<>();
		String hash = makeHash(content, baseName, blankNodeMap);
		RDFWriter writer = Rio.createWriter(format, out);
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
