package net.trustyuri;

/**
 * An interface for artifact codes.
 *
 */
public interface ArtifactCode {

    /**
     * Returns the module ID associated with the artifact code.
     *
     * @return the module ID
     */
    TrustyUriModule getModule();

    /**
     * Returns the artifact code as a string. It is a concatenation of the module ID and the data hash.
     *
     * @return the artifact code as a string
     */
    @Override
    String toString();

    /**
     * Creates an ArtifactCode from the given string. Returns null if the string is not a valid artifact code.
     *
     * @param code the string to create the ArtifactCode from
     * @return the ArtifactCode, or null if the string is not a valid artifact code
     */
    static ArtifactCode of(String code) {
        return new ArtifactCodeImpl(code);
    }

    /**
     * Creates an ArtifactCode from the given module and code. Returns null if the module or code is null, or if the code is not valid for the module.
     *
     * @param module the module associated with the artifact code
     * @param code   the data hash part of the artifact code
     * @return the ArtifactCode, or null if the module or code is null, or if the code is not valid for the module
     */
    static ArtifactCode of(TrustyUriModule module, String code) {
        return new ArtifactCodeImpl(module, code);
    }

}
