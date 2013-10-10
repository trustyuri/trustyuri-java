package ch.tkuhn.hashuri;

import org.openrdf.model.URI;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.bind.DatatypeConverter;

public class HashUriUtils {

	private static final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();

	public static String getHashUriDataPart(String s) {
		if (!s.matches("(.*[^A-Za-z0-9\\-_]|)[A-Za-z0-9\\-_]{25,}(\\.[A-Za-z0-9\\-_]{0,20})?")) {
			return null;
		}
		return s.replaceFirst("^(.*[^A-Za-z0-9\\-_]|)([A-Za-z0-9\\-_]{25,})(\\.[A-Za-z0-9\\-_]{0,20})?$", "$2");
	}

	public static boolean isPotentialHashUri(URI uri) {
		String d = getHashUriDataPart(uri.toString());
		if (d == null) return false;
		String id = d.substring(0, 2);
		HashUriModule module = ModuleDirectory.getModule(id);
		if (module == null) return false;
		int l = d.substring(2).length();
		return l == module.getHashLength();
	}

	public static String getNiUri(String s) {
		return getNiUri(s, true);
	}

	public static String getNiUri(String s, boolean withAuthority) {
		String d = getHashUriDataPart(s);
		if (d == null) return null;
		String moduleId = d.substring(0, 2);
		String hash = d.substring(2);
		HashUriModule module = ModuleDirectory.getModule(moduleId);
		String tail = "/" + module.getAlgorithmId() + ";" + hash + "?module=" + moduleId;
		if (withAuthority) {
			try {
				String autority = (new java.net.URI(s)).getAuthority().toString();
				return "ni://" + autority + tail;
			} catch (Exception ex) {}
		}
		return "ni://" + tail;
	}

	public static String getBase64(byte[] bytes) {
		String h = DatatypeConverter.printBase64Binary(bytes);
		h = h.replaceFirst("=*$", "");
		h = h.replace('+', '-');
		h = h.replace('/', '_');
		return h;
	}

	public static String getMimetype(String filename) {
		return mimeMap.getContentType(filename);
	}

}
