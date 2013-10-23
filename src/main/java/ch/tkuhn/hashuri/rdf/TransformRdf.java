package ch.tkuhn.hashuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;

import ch.tkuhn.hashuri.HashUriResource;
import ch.tkuhn.nanopub.CustomTrigWriterFactory;

public class TransformRdf {

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

		content = RdfPreprocessor.run(content, baseURI);
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
		Map<String,String> ns = makeNamespaceMap(content.getStatements(), baseURI, hash);
		content.propagate(new HashAdder(baseURI, hash, writer, ns));
		out.close();
		return RdfUtils.getHashURI(baseURI, baseURI, hash, null);
	}

	public static URI transform(RdfFileContent content, RDFHandler handler, String baseName) throws Exception {
		URI baseURI = getBaseURI(baseName);
		content = RdfPreprocessor.run(content, baseURI);
		String hash = RdfHasher.makeHash(content.getStatements());
		Map<String,String> ns = makeNamespaceMap(content.getStatements(), baseURI, hash);
		content.propagate(new HashAdder(baseURI, hash, handler, ns));
		return RdfUtils.getHashURI(baseURI, baseURI, hash, null);
	}

	public static URI transform(InputStream in, RDFFormat format, OutputStream out, String baseName) throws Exception {
		URI baseURI = getBaseURI(baseName);
		RdfFileContent content = RdfUtils.load(in, format);
		content = RdfPreprocessor.run(content, baseURI);
		String hash = RdfHasher.makeHash(content.getStatements());
		RDFWriter writer = Rio.createWriter(format, out);
		Map<String,String> ns = makeNamespaceMap(content.getStatements(), baseURI, hash);
		HashAdder replacer = new HashAdder(baseURI, hash, writer, ns);
		content.propagate(replacer);
		out.close();
		return RdfUtils.getHashURI(baseURI, baseURI, hash, null);
	}

	private static URI getBaseURI(String baseName) {
		URI baseURI = null;
		if (baseName.indexOf("/") > 0) {
			baseURI = new URIImpl(baseName);
		}
		return baseURI;
	}

	private static Map<String,String> makeNamespaceMap(List<Statement> statements, URI baseURI, String hash) {
		Map<String,String> ns = new HashMap<>();
		if (baseURI == null) return ns;
		String u = RdfUtils.getHashURIString(baseURI, hash);
		ns.put("this", u);
		for (Statement st : statements) {
			addToNamespaceMap(st.getSubject(), baseURI, hash, ns);
			addToNamespaceMap(st.getPredicate(), baseURI, hash, ns);
			addToNamespaceMap(st.getObject(), baseURI, hash, ns);
			addToNamespaceMap(st.getContext(), baseURI, hash, ns);
		}
		return ns;
	}

	private static void addToNamespaceMap(Value v, URI baseURI, String hash, Map<String,String> ns) {
		if (!(v instanceof URI)) return;
		String nanopubURI = RdfUtils.getHashURIString(baseURI, hash);
		String s = v.toString().replaceAll(" ", hash);
		if (!s.startsWith(nanopubURI)) return;
		String suffix = s.substring(nanopubURI.length());
		if (suffix.matches("\\.\\..*")) {
			ns.put("blank", nanopubURI + "..");
		} else if (suffix.matches("[^A-Za-z0-9\\-_].*")) {
			ns.put("sub", nanopubURI + suffix.charAt(0));
		}
	}

}
