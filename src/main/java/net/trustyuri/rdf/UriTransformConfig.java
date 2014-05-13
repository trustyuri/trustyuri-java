package net.trustyuri.rdf;

public class UriTransformConfig {

	public static UriTransformConfig DOT_CONFIG = new UriTransformConfig('.', false, '.', false, '.');
	public static UriTransformConfig HASH_CONFIG = new UriTransformConfig('.', false, '#', true, '_');

	private static UriTransformConfig defaultConfig = HASH_CONFIG;

	public static UriTransformConfig getDefault() {
		return defaultConfig;
	}

	public static void setDefault(UriTransformConfig defaultConfig) {
		UriTransformConfig.defaultConfig = defaultConfig;
	}

	private char preChar, postChar, bnodeChar;
	private boolean preForced, postForced;

	public UriTransformConfig(char preChar, boolean preForced, char postChar, boolean postForced, char bnodeChar) {
		this.preChar = preChar;
		this.preForced = preForced;
		this.postChar = postChar;
		this.postForced = postForced;
		this.bnodeChar = bnodeChar;
	}

	public char getPreHashChar() {
		return preChar;
	}

	public boolean isPreHashCharForced() {
		return preForced;
	}

	public char getPostHashChar() {
		return postChar;
	}

	public boolean isPostHashCharForced() {
		return postForced;
	}

	public char getBnodeChar() {
		return bnodeChar;
	}

}
