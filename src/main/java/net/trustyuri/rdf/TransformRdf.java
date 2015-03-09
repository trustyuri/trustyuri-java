package net.trustyuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

public class TransformRdf {

	// TODO Use RB module by default if trusty URI represents a single RDF graph

	public static void main(String[] args) throws IOException, TrustyUriException {
		File inputFile = new File(args[0]);
		String baseName = null;
		if (args.length > 1) {
			baseName = args[1];
		}
		transform(inputFile, baseName);
	}

	public static URI transform(File inputFile, String baseName)
			throws IOException, TrustyUriException {
		if (baseName == null) {
			baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
		}
		RdfFileContent content = RdfUtils.load(new TrustyUriResource(inputFile));
		URI baseUri = getBaseURI(baseName);
		String name = baseName;
		if (baseName.indexOf("/") > 0) {
			name = baseName.replaceFirst("^.*[^A-Za-z0-9.\\-_]([A-Za-z0-9.\\-_]*)$", "$1");
		}

		content = RdfPreprocessor.run(content, baseUri);
		String artifactCode = RdfHasher.makeArtifactCode(content.getStatements());
		RDFFormat format = content.getOriginalFormat();
		String fileName = name;
		String ext = "";
		if (!format.getFileExtensions().isEmpty()) {
			ext = "." + format.getFileExtensions().get(0);
		}
		if (fileName.length() == 0) {
			fileName = artifactCode + ext;
		} else {
			fileName += "." + artifactCode + ext;
		}
		OutputStream out;
		if (inputFile.getName().matches(".*\\.(gz|gzip)")) {
			out = new GZIPOutputStream(new FileOutputStream(new File(inputFile.getParent(), fileName + ".gz")));
		} else {
			out = new FileOutputStream(new File(inputFile.getParent(), fileName));
		}
		RDFWriter writer = Rio.createWriter(format, out);
		URI uri = includeArtifactCode(content, artifactCode, baseUri, writer);
		out.close();
		return uri;
	}

	public static URI transform(RdfFileContent content, RDFHandler handler, String baseName)
			throws TrustyUriException {
		URI baseUri = getBaseURI(baseName);
		content = RdfPreprocessor.run(content, baseUri);
		URI uri = transformPreprocessed(content, baseUri, handler);
		return uri;
	}

	public static URI transform(InputStream in, RDFFormat format, OutputStream out, String baseName)
			throws IOException, TrustyUriException {
		URI baseUri = getBaseURI(baseName);
		RdfFileContent content = RdfUtils.load(in, format);
		content = RdfPreprocessor.run(content, baseUri);
		RDFWriter writer = Rio.createWriter(format, out);
		URI uri = transformPreprocessed(content, baseUri, writer);
		out.close();
		return uri;
	}

	public static URI transformPreprocessed(RdfFileContent preprocessedContent, URI baseUri, RDFWriter writer)
			throws TrustyUriException {
		String artifactCode = RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
		URI uri = includeArtifactCode(preprocessedContent, artifactCode, baseUri, writer);
		return uri;
	}

	public static URI transformPreprocessed(RdfFileContent preprocessedContent, URI baseUri, RDFHandler handler)
			throws TrustyUriException {
		String artifactCode = RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
		URI uri = includeArtifactCode(preprocessedContent, artifactCode, baseUri, handler);
		return uri;
	}

	private static URI includeArtifactCode(RdfFileContent preprocessedContent, String artifactCode, URI baseUri, Object writerOrHandler)
			throws TrustyUriException {
		Map<String,String> ns = makeNamespaceMap(preprocessedContent.getStatements(), baseUri, artifactCode);
		HashAdder hashAdder;
		if (writerOrHandler instanceof RDFWriter) {
			hashAdder = new HashAdder(baseUri, artifactCode, (RDFWriter) writerOrHandler, ns);
		} else {
			hashAdder = new HashAdder(baseUri, artifactCode, (RDFHandler) writerOrHandler, ns);
		}
		try {
			preprocessedContent.propagate(hashAdder);
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
		return RdfUtils.getTrustyUri(baseUri, artifactCode);
		
	}

	static URI getBaseURI(String baseName) {
		URI baseURI = null;
		if (baseName.indexOf("://") > 0) {
			baseURI = new URIImpl(baseName);
		}
		return baseURI;
	}

	static Map<String,String> makeNamespaceMap(List<Statement> statements, URI baseURI, String artifactCode) {
		Map<String,String> ns = new HashMap<String,String>();
		if (baseURI == null) return ns;
		String u = RdfUtils.getTrustyUriString(baseURI, artifactCode);
		ns.put("this", u);
		for (Statement st : statements) {
			addToNamespaceMap(st.getSubject(), baseURI, artifactCode, ns);
			addToNamespaceMap(st.getPredicate(), baseURI, artifactCode, ns);
			addToNamespaceMap(st.getObject(), baseURI, artifactCode, ns);
			addToNamespaceMap(st.getContext(), baseURI, artifactCode, ns);
		}
		return ns;
	}

	static void addToNamespaceMap(Value v, URI baseURI, String artifactCode, Map<String,String> ns) {
		if (!(v instanceof URI)) return;
		String uri = RdfUtils.getTrustyUriString(baseURI, artifactCode);
		String s = v.toString().replace(" ", artifactCode);
		if (!s.startsWith(uri)) return;
		String suffix = s.substring(uri.length());
		if (suffix.length() > 2 && suffix.charAt(0) == RdfUtils.getPostAcChar(baseURI) && suffix.charAt(1) == RdfUtils.bnodeChar &&
				!(RdfUtils.bnodeChar + "").matches("[A-Za-z0-9\\-_]")) {
			ns.put("node", uri + "..");
		} else if (suffix.matches("[^A-Za-z0-9\\-_].*")) {
			ns.put("sub", uri + suffix.charAt(0));
		}
	}

}
