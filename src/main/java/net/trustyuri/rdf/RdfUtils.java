package net.trustyuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import net.trustyuri.TrustyUriUtils;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFaParserSettings;

public class RdfUtils {

	private RdfUtils() {}  // no instances allowed

	public static String getTrustyUriString(URI baseUri, String artifactCode, String suffix) {
		UriTransformConfig c = UriTransformConfig.getDefault();
		String s = expandBaseUri(baseUri) + artifactCode;
		if (suffix != null) {
			if (suffix.startsWith(c.getBnodeChar() + "")) {
				// Duplicate bnode character for escaping:
				s += c.getPostHashChar() + c.getBnodeChar() + suffix;
			} else if (!c.isPostHashCharForced() && suffix.matches("[^A-Za-z0-9\\-_].*")) {
				s += suffix;
			} else {
				s += c.getPostHashChar() + suffix;
			}
		}
		return s;
	}

	public static String getTrustyUriString(URI baseUri, String artifactCode) {
		return getTrustyUriString(baseUri, artifactCode, null);
	}

	public static URI getTrustyUri(URI baseUri, String artifactCode, String suffix) {
		if (baseUri == null) return null;
		return new URIImpl(getTrustyUriString(baseUri, artifactCode, suffix));
	}

	public static URI getTrustyUri(URI baseUri, String artifactCode) {
		if (baseUri == null) return null;
		return new URIImpl(getTrustyUriString(baseUri, artifactCode, null));
	}

	public static URI getPreUri(Resource resource, URI baseUri, Map<String,Integer> bnodeMap, boolean frozen) {
		if (resource == null) {
			throw new RuntimeException("Resource is null");
		} else if (resource instanceof URI) {
			URI plainUri = (URI) resource;
			if (plainUri.toString().matches(".*(\\n|\\t).*")) {
				throw new RuntimeException("Newline or tab character in URI: " + plainUri.toString());
			}
			// TODO Add option to disable suffixes appended to trusty URIs
			String suffix = getSuffix(plainUri, baseUri);
			if (suffix == null && !plainUri.equals(baseUri)) {
				return plainUri;
			} else if (frozen) {
				return null;
			} else if (TrustyUriUtils.isPotentialTrustyUri(plainUri)) {
				return plainUri;
			} else {
				return getTrustyUri(baseUri, " ", suffix);
			}
		} else {
			if (frozen) {
				return null;
			} else {
				return getSkolemizedUri((BNode) resource, baseUri, bnodeMap);
			}
		}
	}

	private static URI getSkolemizedUri(BNode bnode, URI baseUri, Map<String,Integer> bnodeMap) {
		int n = getBlankNodeNumber(bnode, bnodeMap);
		UriTransformConfig c = UriTransformConfig.getDefault();
		return new URIImpl(expandBaseUri(baseUri) + " " + c.getPostHashChar() + c.getBnodeChar() + n);
	}

	private static String getSuffix(URI plainUri, URI baseUri) {
		if (baseUri == null) return null;
		String b = baseUri.toString();
		String p = plainUri.toString();
		if (p.equals(b)) {
			return null;
		} else if (p.startsWith(b)) {
			return p.substring(b.length());
		} else {
			return null;
		}
	}

	public static String normalize(URI uri, String artifactCode) {
		String s = uri.toString();
		if (s.indexOf('\n') > -1 || s.indexOf('\t') > -1) {
			throw new RuntimeException("Newline or tab character in URI: " + s);
		}
		if (artifactCode == null) return s;
		return s.replace(artifactCode, " ");
	}

	private static int getBlankNodeNumber(BNode blankNode, Map<String,Integer> bnodeMap) {
		String id = blankNode.getID();
		Integer n = bnodeMap.get(id);
		if (n == null) {
			n = bnodeMap.size()+1;
			bnodeMap.put(id, n);
		}
		return n;
	}

	private static String expandBaseUri(URI baseUri) {
		UriTransformConfig c = UriTransformConfig.getDefault();
		String s = baseUri.toString();
		if (s.matches(".*[A-Za-z0-9\\-_]") || c.isPreHashCharForced()) {
			s += c.getPreHashChar();
		}
		return s;
	}

	public static RdfFileContent load(InputStream in, RDFFormat format) throws IOException, TrustyUriException {
		RDFParser p = getParser(format);
		RdfFileContent content = new RdfFileContent(format);
		p.setRDFHandler(content);
		try {
			p.parse(in, "");
		} catch (OpenRDFException ex) {
			throw new TrustyUriException(ex);
		}
		in.close();
		return content;
	}

	public static RDFParser getParser(RDFFormat format) {
		RDFParser p = Rio.createParser(format);
		p.getParserConfig().set(RDFaParserSettings.FAIL_ON_RDFA_UNDEFINED_PREFIXES, true);
		return p;
	}

	public static RdfFileContent load(TrustyUriResource r) throws IOException, TrustyUriException {
		return load(r.getInputStream(), r.getFormat(RDFFormat.TURTLE));
	}

	public static void fixTrustyRdf(File file) throws IOException, TrustyUriException {
		TrustyUriResource r = new TrustyUriResource(file);
		RdfFileContent content = RdfUtils.load(r);
		String oldArtifactCode = r.getArtifactCode();
		content = RdfPreprocessor.run(content, oldArtifactCode);
		String newArtifactCode = createArtifactCode(content, oldArtifactCode.startsWith("RB"));
		content = processNamespaces(content, oldArtifactCode, newArtifactCode);
		OutputStream out;
		String filename = r.getFilename().replace(oldArtifactCode, newArtifactCode);
		if (filename.matches(".*\\.(gz|gzip)")) {
			out = new GZIPOutputStream(new FileOutputStream(new File("fixed." + filename)));
		} else {
			out = new FileOutputStream(new File("fixed." + filename));
		}
		RDFWriter writer = Rio.createWriter(r.getFormat(RDFFormat.TRIG), out);
		TransformRdf.transformPreprocessed(content, null, writer);
	}

	public static void fixTrustyRdf(RdfFileContent content, String oldArtifactCode, RDFHandler writer)
			throws TrustyUriException {
		content = RdfPreprocessor.run(content, oldArtifactCode);
		String newArtifactCode = createArtifactCode(content, oldArtifactCode.startsWith("RB"));
		content = processNamespaces(content, oldArtifactCode, newArtifactCode);
		TransformRdf.transformPreprocessed(content, null, writer);
	}

	private static String createArtifactCode(RdfFileContent preprocessedContent, boolean graphModule) throws TrustyUriException {
		if (graphModule) {
			return RdfHasher.makeGraphArtifactCode(preprocessedContent.getStatements());
		} else {
			return RdfHasher.makeArtifactCode(preprocessedContent.getStatements());
		}
	}

	private static RdfFileContent processNamespaces(RdfFileContent content, String oldArtifactCode, String newArtifactCode) {
		try {
			RdfFileContent contentOut = new RdfFileContent(content.getOriginalFormat());
			content.propagate(new NamespaceProcessor(oldArtifactCode, newArtifactCode, contentOut));
			return contentOut;
		} catch (RDFHandlerException ex) {
			ex.printStackTrace();
		}
		return content;
	}


	private static class NamespaceProcessor implements RDFHandler {

		private RDFHandler handler;
		private String oldArtifactCode, newArtifactCode;

		public NamespaceProcessor(String oldArtifactCode, String newArtifactCode, RDFHandler handler) {
			this.handler = handler;
			this.oldArtifactCode = oldArtifactCode;
			this.newArtifactCode = newArtifactCode;
		}

		@Override
		public void startRDF() throws RDFHandlerException {
			handler.startRDF();
		}

		@Override
		public void endRDF() throws RDFHandlerException {
			handler.endRDF();
		}

		@Override
		public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
			handler.handleNamespace(prefix, uri.replace(oldArtifactCode, newArtifactCode));
		}

		@Override
		public void handleStatement(Statement st) throws RDFHandlerException {
			handler.handleStatement(st);
		}

		@Override
		public void handleComment(String comment) throws RDFHandlerException {
			handler.handleComment(comment);
		}

	}

}
