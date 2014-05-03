package net.trustyuri;

import java.io.IOException;

import org.openrdf.model.URI;

public interface TrustyUriModule {

	public String getModuleId();

	public String getAlgorithmId();

	public int getDataPartLength();

	public boolean hasCorrectHash(TrustyUriResource resource) throws IOException, TrustyUriException;

	public boolean matches(URI uri);

}
