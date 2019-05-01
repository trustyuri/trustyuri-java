package net.trustyuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

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

	public static IRI transform(File inputFile, String baseName)
			throws IOException, TrustyUriException {
		if (baseName == null) {
			baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
		}
		RdfFileContent content = RdfUtils.load(new TrustyUriResource(inputFile));
		IRI baseUri = getBaseURI(baseName);
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
		RDFWriter writer = Rio.createWriter(format, new OutputStreamWriter(out, Charset.forName("UTF-8")));
		IRI uri = includeArtifactCode(content, artifactCode, baseUri, writer);
		out.close();
		return uri;
	}

	public static IRI transform(RdfFileContent content, RDFHandler handler, String baseName)
			throws TrustyUriException {
		IRI baseUri = getBaseURI(baseName);
		content = RdfPreprocessor.run(content, baseUri);
		IRI uri = transformPreprocessed(content, baseUri, handler);
		return uri;
	}

	public static Map<Resource,IRI> transformAndGetMap(RdfFileContent content, RDFHandler handler, String baseName)
			throws TrustyUriException {
		IRI baseUri = getBaseURI(baseName);
		RdfFileContent newContent = new RdfFileContent(content.getOriginalFormat());
		RdfPreprocessor rp = new RdfPreprocessor(newContent, baseUri);
		try {
			content.propagate(rp);
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
		String artifactCode = RdfHasher.makeArtifactCode(newContent.getStatements());
		includeArtifactCode(newContent, artifactCode, baseUri, handler);
		return finalizeTransformMap(rp.getTransformMap(), artifactCode);
	}

	public static IRI transform(InputStream in, RDFFormat format, OutputStream out, String baseName)
			throws IOException, TrustyUriException {
		IRI baseUri = getBaseURI(baseName);
		RdfFileContent content = RdfUtils.load(in, format);
		content = RdfPreprocessor.run(content, baseUri);
		RDFWriter writer = Rio.createWriter(format, new OutputStreamWriter(out, Charset.forName("UTF-8")));
		IRI uri = transformPreprocessed(content, baseUri, writer);
		out.close();
		return uri;
	}

	public static IRI transformPreprocessed(RdfFileContent preprocessedContent, IRI baseUri, RDFWriter writer)
			throws TrustyUriException {
		String artifactCode = RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
		IRI uri = includeArtifactCode(preprocessedContent, artifactCode, baseUri, writer);
		return uri;
	}

	public static IRI transformPreprocessed(RdfFileContent preprocessedContent, IRI baseUri, RDFHandler handler)
			throws TrustyUriException {
		String artifactCode = RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
		IRI uri = includeArtifactCode(preprocessedContent, artifactCode, baseUri, handler);
		return uri;
	}

	private static IRI includeArtifactCode(RdfFileContent preprocessedContent, String artifactCode, IRI baseUri, Object writerOrHandler)
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

	static IRI getBaseURI(String baseName) {
		IRI baseURI = null;
		if (baseName.indexOf("://") > 0) {
			baseURI = SimpleValueFactory.getInstance().createIRI(baseName);
		}
		return baseURI;
	}

	static Map<String,String> makeNamespaceMap(List<Statement> statements, IRI baseURI, String artifactCode) {
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

	static void addToNamespaceMap(Value v, IRI baseURI, String artifactCode, Map<String,String> ns) {
		if (!(v instanceof IRI)) return;
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

	static Map<Resource,IRI> finalizeTransformMap(Map<Resource,IRI> transformMap, String artifactCode) {
		Map<Resource,IRI> finalMap = new HashMap<>();
		for (Resource r : transformMap.keySet()) {
			String s = transformMap.get(r).stringValue().replaceFirst(" ", artifactCode);
			finalMap.put(r, SimpleValueFactory.getInstance().createIRI(s));
		}
		return finalMap;
	}

}
