package net.trustyuri.rdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import net.trustyuri.TrustyUriUtils;

public class RdfUtils {

	private RdfUtils() {}  // no instances allowed

	public static String getTrustyUriString(IRI baseUri, String artifactCode, String suffix, TransformRdfSetting setting) {
		String s = expandBaseUri(baseUri, setting) + artifactCode;
		if (suffix != null) {
			suffix = suffix.replace("#", "%23");
			if (suffix.startsWith(setting.getBnodeChar() + "")) {
				// Duplicate bnode character for escaping:
				s += "" + getPostAcChar(baseUri, setting) + setting.getBnodeChar() + suffix;
			} else {
				s += "" + getPostAcChar(baseUri, setting) + suffix;
			}
		}
		return s;
	}

	public static String getTrustyUriString(IRI baseUri, String artifactCode, TransformRdfSetting setting) {
		return getTrustyUriString(baseUri, artifactCode, null, setting);
	}

	public static IRI getTrustyUri(IRI baseUri, String artifactCode, String suffix, TransformRdfSetting setting) {
		if (baseUri == null) return null;
		return SimpleValueFactory.getInstance().createIRI(getTrustyUriString(baseUri, artifactCode, suffix, setting));
	}

	public static IRI getTrustyUri(IRI baseUri, String artifactCode, TransformRdfSetting setting) {
		if (baseUri == null) return null;
		return SimpleValueFactory.getInstance().createIRI(getTrustyUriString(baseUri, artifactCode, setting));
	}

	public static IRI getPreUri(Resource resource, IRI baseUri, Map<String,Integer> bnodeMap, boolean frozen, TransformRdfSetting setting) {
		if (resource == null) {
			throw new RuntimeException("Resource is null");
		} else if (resource instanceof IRI) {
			IRI plainUri = (IRI) resource;
			checkUri(plainUri);
			// TODO Add option to disable suffixes appended to trusty URIs
			String suffix = getSuffix(plainUri, baseUri);
			if (suffix == null && !plainUri.equals(baseUri)) {
				return plainUri;
			} else if (frozen) {
				return null;
			} else if (TrustyUriUtils.isPotentialTrustyUri(plainUri)) {
				return plainUri;
			} else {
				return getTrustyUri(baseUri, " ", suffix, setting);
			}
		} else {
			if (frozen) {
				return null;
			} else {
				return getSkolemizedUri((BNode) resource, baseUri, bnodeMap, setting);
			}
		}
	}

	public static void checkUri(IRI uri) {
		try {
			// Raise error if not well-formed
			new java.net.URI(uri.stringValue());
		} catch (URISyntaxException ex) {
			throw new RuntimeException("Malformed URI: " + uri.stringValue(), ex);
		}
	}

	public static char getPostAcChar(IRI baseUri, TransformRdfSetting setting) {
		if (setting.getPostAcChar() == '#' && baseUri.stringValue().contains("#")) {
			return setting.getPostAcFallbackChar();
		}
		return setting.getPostAcChar();
	}

	private static IRI getSkolemizedUri(BNode bnode, IRI baseUri, Map<String,Integer> bnodeMap, TransformRdfSetting setting) {
		int n = getBlankNodeNumber(bnode, bnodeMap);
		return SimpleValueFactory.getInstance().createIRI(expandBaseUri(baseUri, setting) + " " + getPostAcChar(baseUri, setting) + setting.getBnodeChar() + n);
	}

	private static String getSuffix(IRI plainUri, IRI baseUri) {
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

	public static String normalize(IRI uri, String artifactCode) {
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

	private static String expandBaseUri(IRI baseUri, TransformRdfSetting setting) {
		String s = baseUri.toString();
		s = s.replaceFirst("ARTIFACTCODE-PLACEHOLDER[\\.#/]?$", "");
		if (s.matches(".*[A-Za-z0-9\\-_]")) {
			s += setting.getPreAcChar();
		}
		return s;
	}

	public static RdfFileContent load(InputStream in, RDFFormat format) throws IOException, TrustyUriException {
		RDFParser p = getParser(format);
		RdfFileContent content = new RdfFileContent(format);
		p.setRDFHandler(content);
		try {
			p.parse(new InputStreamReader(in, Charset.forName("UTF-8")), "");
		} catch (RDF4JException ex) {
			ex.printStackTrace();
			throw new TrustyUriException(ex);
		}
		in.close();
		return content;
	}

	public static RDFParser getParser(RDFFormat format) {
		RDFParser p = Rio.createParser(format);
		p.getParserConfig().addNonFatalError(BasicParserSettings.VERIFY_URI_SYNTAX);
		p.getParserConfig().set(BasicParserSettings.NAMESPACES, new HashSet<Namespace>());
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
		RDFWriter writer = Rio.createWriter(r.getFormat(RDFFormat.TRIG), new OutputStreamWriter(out, Charset.forName("UTF-8")));
		TransformRdf.transformPreprocessed(content, null, writer, null);
	}

	public static void fixTrustyRdf(RdfFileContent content, String oldArtifactCode, RDFHandler writer)
			throws TrustyUriException {
		content = RdfPreprocessor.run(content, oldArtifactCode);
		String newArtifactCode = createArtifactCode(content, oldArtifactCode.startsWith("RB"));
		content = processNamespaces(content, oldArtifactCode, newArtifactCode);
		TransformRdf.transformPreprocessed(content, null, writer, null);
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
