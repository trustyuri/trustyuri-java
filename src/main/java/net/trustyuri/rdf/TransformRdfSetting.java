package net.trustyuri.rdf;

public class TransformRdfSetting {

	private final char bnodeChar;
	private final char preAcChar;
	private final char postAcChar;
	private final char postAcFallbackChar;

	public static final TransformRdfSetting defautSetting = new TransformRdfSetting('_', '.', '#', '.');
	
	public TransformRdfSetting(char bnodeChar, char preAcChar, char postAcChar, char postAcFallbackChar) {
		this.bnodeChar = bnodeChar;
		this.preAcChar = preAcChar;
		this.postAcChar = postAcChar;
		this.postAcFallbackChar = postAcFallbackChar;
	}

	public char getBnodeChar() {
		return bnodeChar;
	}

	public char getPreAcChar() {
		return preAcChar;
	}

	public char getPostAcChar() {
		return postAcChar;
	}

	public char getPostAcFallbackChar() {
		return postAcFallbackChar;
	}

}
