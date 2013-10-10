package ch.tkuhn.hashuri.file;

import ch.tkuhn.hashuri.HashUriModule;
import ch.tkuhn.hashuri.HashUriResource;

public class FileModule implements HashUriModule {

	public static final String MODULE_ID = "FA";

	@Override
	public String getModuleId() {
		return MODULE_ID;
	}

	@Override
	public String getAlgorithmId() {
		return "sha-256";
	}

	@Override
	public int getHashLength() {
		return 43;
	}

	@Override
	public boolean hasCorrectHash(HashUriResource r) throws Exception {
		FileHasher hasher = new FileHasher();
		String h = hasher.makeHash(r.getInputStream());
		return r.getHash().equals(h);
	}

}
