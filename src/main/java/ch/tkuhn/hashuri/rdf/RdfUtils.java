package ch.tkuhn.hashuri.rdf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

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

import ch.tkuhn.hashuri.HashUriResource;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubUtils;

public class RdfUtils {

	private RdfUtils() {}  // no instances allowed

	public static String getHashURIString(URI baseURI, String hash, String suffix) {
		String s = expandBaseURI(baseURI) + hash;
		if (suffix != null) {
			if (suffix.startsWith(".")) {
				// Make three dots, as two dots are reserved for blank nodes
				s += ".." + suffix;
			} else if (suffix.matches("[^A-Za-z0-9\\-_].*")) {
				s += suffix;
			} else {
				s += "." + suffix;
			}
		}
		return s;
	}

	public static String getHashURIString(URI baseURI, String hash) {
		return getHashURIString(baseURI, hash, null);
	}

	public static URI getHashURI(Resource resource, URI baseURI, String hash, Map<String,Integer> blankNodeMap) {
		if (resource == null) {
			return null;
		} else if (resource instanceof URI) {
			URI plainURI = (URI) resource;
			String suffix = getSuffix(plainURI, baseURI);
			if (suffix == null && !plainURI.equals(baseURI)) {
				return plainURI;
			}
			return new URIImpl(getHashURIString(baseURI, hash, suffix));
		} else {
			BNode blankNode = (BNode) resource;
			int n = getBlankNodeNumber(blankNode, blankNodeMap);
			return new URIImpl(expandBaseURI(baseURI) + hash + ".." + n);
		}
	}

	private static String getSuffix(URI plainURI, URI baseURI) {
		if (baseURI == null) return null;
		String b = baseURI.toString();
		String p = plainURI.toString();
		if (p.equals(b)) {
			return null;
		} else if (p.startsWith(b)) {
			return p.substring(b.length());
		} else {
			return null;
		}
	}

	public static String normalize(URI uri, String hash) {
		if (hash == null) return uri.toString();
		return uri.toString().replaceAll(hash, " ");
	}

	public static int getBlankNodeNumber(BNode blankNode, Map<String,Integer> blankNodeMap) {
		String id = blankNode.getID();
		Integer n = blankNodeMap.get(id);
		if (n == null) {
			n = blankNodeMap.size()+1;
			blankNodeMap.put(id, n);
		}
		return n;
	}

	private static String expandBaseURI(URI baseURI) {
		String s = baseURI.toString();
		if (s.matches(".*[A-Za-z0-9\\-_]")) {
			s += ".";
		}
		return s;
	}

	public static RdfFileContent load(InputStream in, RDFFormat format) throws Exception {
		RDFParser p = Rio.createParser(format);
		RdfFileContent content = new RdfFileContent(format);
		p.setRDFHandler(content);
		p.parse(in, "");
		in.close();
		return content;
	}

	public static RdfFileContent load(InputStream in, RDFFormat format, RdfFilter filter) throws Exception {
		RDFParser p = Rio.createParser(format);
		RdfFileContent content = new RdfFileContent(format, filter);
		p.setRDFHandler(content);
		p.parse(in, "");
		in.close();
		return content;
	}

	public static RdfSummary loadSummary(InputStream in, RDFFormat format, URI baseUri,
			Map<String,Integer> blankNodeMap) throws Exception {
		RDFParser p = Rio.createParser(format);
		RdfSummary summary = new RdfSummary(format, baseUri, blankNodeMap);
		p.setRDFHandler(summary);
		p.parse(in, "");
		in.close();
		return summary;
	}

	public static RdfFileContent load(HashUriResource r) throws Exception {
		return load(r.getInputStream(), r.getFormat(RDFFormat.TURTLE));
	}

	public static RdfFileContent load(HashUriResource r, RdfFilter filter) throws Exception {
		return load(r.getInputStream(), r.getFormat(RDFFormat.TURTLE), filter);
	}

	public static RdfSummary loadSummary(HashUriResource r, URI baseUri,
			Map<String,Integer> blankNodeMap) throws Exception {
		return loadSummary(r.getInputStream(), r.getFormat(RDFFormat.TURTLE), baseUri, blankNodeMap);
	}

	public static void writeNanopub(Nanopub nanopub, OutputStream out, RDFFormat format)
			throws RDFHandlerException {
		RDFWriter writer = Rio.createWriter(format, out);
		writer.startRDF();
		String s = nanopub.getUri().toString();
		writer.handleNamespace("this", s);
		writer.handleNamespace("sub", s + ".");
		writer.handleNamespace("blank", s + "..");
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
