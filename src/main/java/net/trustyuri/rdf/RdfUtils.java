package net.trustyuri.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFaParserSettings;

public class RdfUtils {

	private RdfUtils() {}  // no instances allowed

	public static String getTrustyUriString(URI baseUri, String hash, String suffix) {
		UriTransformConfig c = UriTransformConfig.getDefault();
		String s = expandBaseUri(baseUri) + hash;
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
		RDFParser p = Rio.createParser(format);
		p.getParserConfig().set(RDFaParserSettings.FAIL_ON_RDFA_UNDEFINED_PREFIXES, true);
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

	public static RdfFileContent load(TrustyUriResource r) throws IOException, TrustyUriException {
		return load(r.getInputStream(), r.getFormat(RDFFormat.TURTLE));
	}

	public static void writeNanopub(Nanopub nanopub, OutputStream out, RDFFormat format)
			throws RDFHandlerException {
		RDFWriter writer = Rio.createWriter(format, out);
		writer.startRDF();
		String s = nanopub.getUri().toString();
		writer.handleNamespace("this", s);
		writer.handleNamespace("sub", s + ".");
		writer.handleNamespace("node", s + "..");
		writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		writer.handleNamespace("rdfg", "http://www.w3.org/2004/03/trix/rdfg-1/");
		writer.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		writer.handleNamespace("owl", "http://www.w3.org/2002/07/owl#");
		writer.handleNamespace("dc", "http://purl.org/dc/terms/");
		writer.handleNamespace("pav", "http://swan.mindinformatics.org/ontologies/1.2/pav/");
		writer.handleNamespace("np", "http://www.nanopub.org/nschema#");
		for (Statement st : NanopubUtils.getStatements(nanopub)) {
			writer.handleStatement(st);
		}
		writer.endRDF();
	}

}
