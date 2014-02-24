package net.trustyuri;

public interface TrustyUriModule {

	public String getModuleId();

	public String getAlgorithmId();

	public int getHashLength();

	public boolean hasCorrectHash(TrustyUriResource resource) throws Exception;

}
