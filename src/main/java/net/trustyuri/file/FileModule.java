package net.trustyuri.file;

import net.trustyuri.TrustyUriModule;
import net.trustyuri.TrustyUriResource;

public class FileModule implements TrustyUriModule {

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
	public boolean hasCorrectHash(TrustyUriResource r) throws Exception {
		FileHasher hasher = new FileHasher();
		String h = hasher.makeHash(r.getInputStream());
		return r.getHash().equals(h);
	}

}
