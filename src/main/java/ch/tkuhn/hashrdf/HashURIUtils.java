package ch.tkuhn.hashrdf;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class HashURIUtils {

	private HashURIUtils() {}  // no instances allowed
	
	public static String getHashURIString(URI baseURI, String hash, String suffix) {
		String s = baseURI.toString();
		if (s.matches(".*[A-Za-z0-9\\-_]")) {
			s += ".";
		}
		s += hash;
		if (suffix != null) {
			s += "." + suffix;
		}
		return s;
	}

	public static String getHashURIString(URI baseURI, String hash) {
		return getHashURIString(baseURI, hash, null);
	}

	public static URI getHashURI(URI plainURI, URI baseURI, String hash) {
		String suffix = getSuffix(plainURI, baseURI);
		if (suffix == null && !plainURI.equals(baseURI)) {
			return plainURI;
		}
		return new URIImpl(getHashURIString(baseURI, hash, suffix));
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

}
