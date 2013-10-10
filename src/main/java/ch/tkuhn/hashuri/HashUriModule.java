package ch.tkuhn.hashuri;

public interface HashUriModule {

	public String getModuleId();

	public String getAlgorithmId();

	public int getHashLength();

	public boolean hasCorrectHash(HashUriResource resource) throws Exception;

}
