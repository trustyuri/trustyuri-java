package net.trustyuri.rdf;

/**
 * Settings for transforming RDF content to trusty URI format.
 */
public class TransformRdfSetting {

    private final char bnodeChar;
    private final char preAcChar;
    private final char postAcChar;
    private final char postAcFallbackChar;

    /**
     * Default setting.
     */
    public static final TransformRdfSetting defaultSetting = new TransformRdfSetting('_', '.', '#', '.');

    /**
     * Constructor for TransformRdfSetting.
     *
     * @param bnodeChar          Character to represent blank nodes.
     * @param preAcChar          Character to represent pre-artifact code.
     * @param postAcChar         Character to represent post-artifact code.
     * @param postAcFallbackChar Character to represent post-artifact code fallback when the original post-ac character is not valid in the context.
     */
    public TransformRdfSetting(char bnodeChar, char preAcChar, char postAcChar, char postAcFallbackChar) {
        this.bnodeChar = bnodeChar;
        this.preAcChar = preAcChar;
        this.postAcChar = postAcChar;
        this.postAcFallbackChar = postAcFallbackChar;
    }

    /**
     * Get the character used to represent blank nodes.
     *
     * @return Character for blank nodes.
     */
    public char getBnodeChar() {
        return bnodeChar;
    }

    /**
     * Get the character used to represent pre-artifact code.
     *
     * @return Character for pre-artifact code.
     */
    public char getPreAcChar() {
        return preAcChar;
    }

    /**
     * Get the character used to represent post-artifact code.
     *
     * @return Character for post-artifact code.
     */
    public char getPostAcChar() {
        return postAcChar;
    }

    /**
     * Get the character used to represent post-artifact code fallback when the original post-ac character is not valid in the context.
     *
     * @return Character for post-artifact code fallback.
     */
    public char getPostAcFallbackChar() {
        return postAcFallbackChar;
    }

}
