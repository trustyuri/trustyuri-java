package net.trustyuri;

import org.openrdf.model.URI;

public interface TrustyUriModule {

	public String getModuleId();

	public String getAlgorithmId();

	public int getHashLength();

	public boolean hasCorrectHash(TrustyUriResource resource) throws Exception;

	public boolean matches(URI uri);

}
