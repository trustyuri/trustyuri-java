package ch.tkuhn.hashuri.rdf;

import java.io.InputStream;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

import ch.tkuhn.hashuri.HashUriResource;

public class RdfUtils {

	private RdfUtils() {}  // no instances allowed

	public static String getHashURIString(URI baseURI, String hash, String suffix) {
		String s = expandBaseURI(baseURI) + hash;
		if (suffix != null) {
			if (suffix.startsWith(".")) {
				// Make three dots, as two dots are reserved for blank nodes
				s += ".." + suffix;
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
		if (resource instanceof URI) {
			URI plainURI = (URI) resource;
			String suffix = getSuffix(plainURI, baseURI);
			if (suffix == null && !plainURI.equals(baseURI)) {
				return plainURI;
			}
			return new URIImpl(getHashURIString(baseURI, hash, suffix));
		} else if (resource == null) {
			return null;
		} else {
			BNode blankNode = (BNode) resource;
			int n = getBlankNodeNumber(blankNode, blankNodeMap);
			return new URIImpl(expandBaseURI(baseURI) + hash + ".." + n);
		}
	}

	private static String getSuffix(URI plainURI, URI baseURI) {
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

	public static String normalize(URI plainURI, URI baseURI) {
		String suffix = getSuffix(plainURI, baseURI);
		if (suffix == null && !plainURI.equals(baseURI)) {
			return plainURI.toString();
		}
		return getHashURIString(baseURI, " ", suffix);
	}

	public static String normalize(URI uri, String hash) {
		return uri.toString().replaceAll(hash, " ");
	}

	public static String normalize(BNode blankNode, URI baseURI, Map<String,Integer> blankNodeMap) {
		int n = getBlankNodeNumber(blankNode, blankNodeMap);
		return expandBaseURI(baseURI) + " .." + n;
	}

	public static int getBlankNodeNumber(BNode blankNode, Map<String,Integer> blankNodeMap) {
		String id = blankNode.getID();
		Integer n = blankNodeMap.get(id);
		if (n == null) {
			n = blankNodeMap.size();
			blankNodeMap.put(id, n+1);
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

	public static RdfFileContent load(HashUriResource r) throws Exception {
		RDFFormat format = RDFFormat.forMIMEType(r.getMimetype());
		if (format == null) {
			format = RDFFormat.forFileName(r.getFilename(), RDFFormat.TURTLE);
		}
		return load(r.getInputStream(), format);
	}

}
