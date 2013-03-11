package ch.tkuhn.hashrdf;

import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class HashURIUtils {

	private HashURIUtils() {}  // no instances allowed

	public static String getHashURIString(URI baseURI, String hash, String suffix) {
		String s = expandBaseURI(baseURI) + hash;
		if (suffix != null) {
			if (suffix.startsWith(".")) {
				// Make three dots, as two dot are reserved for blank nodes
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

}
