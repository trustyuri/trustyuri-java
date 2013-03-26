package ch.tkuhn.hashuri;

import java.io.InputStream;

public interface HashUriModule {

	public String getAlgorithmID();

	public boolean isCorrectHash(InputStream in, String hash) throws Exception;

}
