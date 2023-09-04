package net.trustyuri;

import java.security.MessageDigest;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.bind.DatatypeConverter;

import net.trustyuri.rdf.RdfHasher;

public class TrustyUriUtils {

	private static final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();

	public static String getArtifactCode(String trustyUriString) {
		if (!trustyUriString.matches("(.*[^A-Za-z0-9\\-_]|)[A-Za-z0-9\\-_]{25,}(\\.[A-Za-z0-9\\-_\\.]{0,20})?")) {
			return null;
		}
		return trustyUriString.replaceFirst("^(.*[^A-Za-z0-9\\-_]|)([A-Za-z0-9\\-_]{25,})(\\.[A-Za-z0-9\\-_\\.]{0,20})?$", "$2");
	}

	public static boolean isPotentialArtifactCode(String ac) {
		if (ac == null) return false;
		String id = getModuleId(ac);
		TrustyUriModule module = ModuleDirectory.getModule(id);
		if (module == null) return false;
		int l = getDataPart(ac).length();
		return l == module.getDataPartLength();
	}

	public static boolean isPotentialTrustyUri(Object stringObject) {
		return isPotentialArtifactCode(getArtifactCode(stringObject.toString()));
	}

	public static String getModuleId(String artifactCode) {
		return artifactCode.substring(0, 2);
	}

	public static String getDataPart(String artifactCode) {
		return artifactCode.substring(2);
	}

	public static String getNiUri(String s) {
		return getNiUri(s, true);
	}

	public static String getNiUri(String s, boolean withAuthority) {
		String ac = getArtifactCode(s);
		if (ac == null) return null;
		String moduleId = getModuleId(ac);
		// TODO For future modules, hash might not be equal to data part:
		String hash = getDataPart(ac);
		TrustyUriModule module = ModuleDirectory.getModule(moduleId);
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

	public static String getBase64Hash(String s) {
		MessageDigest md = RdfHasher.getDigest();
		md.update(s.getBytes());
		return getBase64(md.digest());
	}

	public static byte[] getBase64Bytes(String base64String) {
		base64String = base64String.replace('-', '+').replace('_', '/');
		while (base64String.length() % 4 > 0) base64String = base64String + "=";
		return DatatypeConverter.parseBase64Binary(base64String);
	}

	public static String getMimetype(String filename) {
		return mimeMap.getContentType(filename);
	}

}
