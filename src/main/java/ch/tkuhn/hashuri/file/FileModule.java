package ch.tkuhn.hashuri.file;

import java.io.InputStream;

import ch.tkuhn.hashuri.HashUriModule;

public class FileModule implements HashUriModule {

	public static final String ALGORITHM_ID = "FA";

	@Override
	public String getAlgorithmID() {
		return ALGORITHM_ID;
	}

	@Override
	public boolean isCorrectHash(InputStream in, String hash) throws Exception {
		FileHasher hasher = new FileHasher();
		String h = hasher.makeHash(in);
		return hash.equals(h);
	}

}
