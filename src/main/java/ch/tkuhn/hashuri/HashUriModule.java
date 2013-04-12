package ch.tkuhn.hashuri;

public interface HashUriModule {

	public String getAlgorithmID();

	public boolean hasCorrectHash(HashUriResource resource) throws Exception;

}
