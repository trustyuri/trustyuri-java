package net.trustyuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.trustyuri.TrustyUriResource;

import org.nanopub.CustomTrigWriterFactory;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;

public class TransformRdf {

	// TODO Use RB module by default if trusty URI represents a single RDF graph

	static {
		RDFWriterRegistry.getInstance().add(new CustomTrigWriterFactory());
	}

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		String baseName;
		if (args.length > 1) {
			baseName = args[1];
		} else {
			baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
		}
		RdfFileContent content = RdfUtils.load(new TrustyUriResource(inputFile));
		transform(content, inputFile.getParent(), baseName);
	}

	public static URI transform(RdfFileContent content, String outputDir, String baseName) throws Exception {
		URI baseUri = getBaseURI(baseName);
		String name = baseName;
		if (baseName.indexOf("/") > 0) {
			name = baseName.replaceFirst("^.*[^A-Za-z0-9.\\-_]([A-Za-z0-9.\\-_]*)$", "$1");
		}

		content = RdfPreprocessor.run(content, baseUri);
		String hash = RdfHasher.makeHash(content.getStatements());
		RDFFormat format = content.getOriginalFormat();
		String fileName = name;
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
		Map<String,String> ns = makeNamespaceMap(content.getStatements(), baseUri, hash);
		content.propagate(new HashAdder(baseUri, hash, writer, ns));
		out.close();
		return RdfUtils.getTrustyUri(baseUri, hash);
	}

	public static URI transform(RdfFileContent content, RDFHandler handler, String baseName) throws Exception {
		URI baseUri = getBaseURI(baseName);
		content = RdfPreprocessor.run(content, baseUri);
		String hash = RdfHasher.makeHash(content.getStatements());
		Map<String,String> ns = makeNamespaceMap(content.getStatements(), baseUri, hash);
		content.propagate(new HashAdder(baseUri, hash, handler, ns));
		return RdfUtils.getTrustyUri(baseUri, hash);
	}

	public static URI transform(InputStream in, RDFFormat format, OutputStream out, String baseName) throws Exception {
		URI baseUri = getBaseURI(baseName);
		RdfFileContent content = RdfUtils.load(in, format);
		content = RdfPreprocessor.run(content, baseUri);
		String hash = RdfHasher.makeHash(content.getStatements());
		RDFWriter writer = Rio.createWriter(format, out);
		Map<String,String> ns = makeNamespaceMap(content.getStatements(), baseUri, hash);
		HashAdder replacer = new HashAdder(baseUri, hash, writer, ns);
		content.propagate(replacer);
		out.close();
		return RdfUtils.getTrustyUri(baseUri, hash);
	}

	static URI getBaseURI(String baseName) {
		URI baseURI = null;
		if (baseName.indexOf("://") > 0) {
			baseURI = new URIImpl(baseName);
		}
		return baseURI;
	}

	static Map<String,String> makeNamespaceMap(List<Statement> statements, URI baseURI, String hash) {
		Map<String,String> ns = new HashMap<>();
		if (baseURI == null) return ns;
		String u = RdfUtils.getTrustyUriString(baseURI, hash);
		ns.put("this", u);
		for (Statement st : statements) {
			addToNamespaceMap(st.getSubject(), baseURI, hash, ns);
			addToNamespaceMap(st.getPredicate(), baseURI, hash, ns);
			addToNamespaceMap(st.getObject(), baseURI, hash, ns);
			addToNamespaceMap(st.getContext(), baseURI, hash, ns);
		}
		return ns;
	}

	static void addToNamespaceMap(Value v, URI baseURI, String hash, Map<String,String> ns) {
		if (!(v instanceof URI)) return;
		String uri = RdfUtils.getTrustyUriString(baseURI, hash);
		String s = v.toString().replace(" ", hash);
		if (!s.startsWith(uri)) return;
		String suffix = s.substring(uri.length());
		if (suffix.matches("\\.\\..*")) {
			ns.put("blank", uri + "..");
		} else if (suffix.matches("[^A-Za-z0-9\\-_].*")) {
			ns.put("sub", uri + suffix.charAt(0));
		}
	}

}
