package net.trustyuri;

import jakarta.activation.MimetypesFileTypeMap;
import jakarta.xml.bind.DatatypeConverter;
import net.trustyuri.rdf.RdfHasher;

import java.net.URI;
import java.security.MessageDigest;

/**
 * A utility class for working with trusty URIs. This class provides static methods for extracting information from trusty URI strings, such as the artifact code. It also includes methods for checking if a string is a potential artifact code or trusty URI, and for converting between base64 strings and byte arrays.
 */
public class TrustyUriUtils {

    private static final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();

    /**
     * Extracts the artifact code from the given trusty URI string. The artifact code is the part of the trusty URI that contains the module identifier and the data hash, and it is typically located at the end of the URI. If the given string does not match the expected format for a trusty URI, this method returns null.
     *
     * @param trustyUriString the string to extract the artifact code from
     * @return the artifact code, or null if the string does not match the expected format for a trusty URI
     */
    public static String getArtifactCode(String trustyUriString) {
        if (!trustyUriString.matches("(.*[^A-Za-z0-9\\-_]|)[A-Za-z0-9\\-_]{25,}(\\.[A-Za-z0-9\\-_\\.]{0,20})?")) {
            return null;
        }
        return trustyUriString.replaceFirst("^(.*[^A-Za-z0-9\\-_]|)([A-Za-z0-9\\-_]{25,})(\\.[A-Za-z0-9\\-_\\.]{0,20})?$", "$2");
    }

    /**
     * Checks if the given string is a potential artifact code, i.e. if it has a valid module identifier and a data part of the correct length for that module.
     *
     * @param potentialArtifactCode the string to check
     * @return true if the string is a potential artifact code, false otherwise
     */
    public static boolean isPotentialArtifactCode(String potentialArtifactCode) {
        if (potentialArtifactCode == null || potentialArtifactCode.isEmpty()) {
            return false;
        }
        String id = getModuleId(potentialArtifactCode);
        TrustyUriModule module = ModuleDirectory.getModule(id);
        if (module == null) {
            return false;
        }
        return getDataPart(potentialArtifactCode).length() == module.getDataPartLength();
    }

    /**
     * Checks if the given string is a potential trusty URI, i.e. if it contains a valid artifact code.
     *
     * @param stringObject the string to check
     * @return true if the string is a potential trusty URI, false otherwise
     */
    public static boolean isPotentialTrustyUri(Object stringObject) {
        return isPotentialArtifactCode(getArtifactCode(stringObject.toString()));
    }

    /**
     * Extracts the module ID from the given artifact code. The module ID is the first two characters of the artifact code.
     *
     * @param artifactCode the artifact code to extract the module ID from
     * @return the module ID, or null if the artifact code is null or too short to contain a module ID
     */
    public static String getModuleId(String artifactCode) {
        return artifactCode.substring(0, 2);
    }

    /**
     * Extracts the data part from the given artifact code. The data part is the substring of the artifact code starting from the third character.
     *
     * @param artifactCode the artifact code to extract the data part from
     * @return the data part, or null if the artifact code is null or too short to contain a data part
     */
    public static String getDataPart(String artifactCode) {
        return artifactCode.substring(2);
    }

    public static String getNiUri(String s) {
        return getNiUri(s, true);
    }

    public static String getNiUri(String s, boolean withAuthority) {
        String ac = getArtifactCode(s);
        if (ac == null) {
            return null;
        }
        String moduleId = getModuleId(ac);
        // TODO For future modules, hash might not be equal to data part:
        String hash = getDataPart(ac);
        TrustyUriModule module = ModuleDirectory.getModule(moduleId);
        String tail = "/" + module.getAlgorithmId() + ";" + hash + "?module=" + moduleId;
        if (withAuthority) {
            try {
                String authority = (new URI(s)).getAuthority();
                return "ni://" + authority + tail;
            } catch (Exception ex) {
            }
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
